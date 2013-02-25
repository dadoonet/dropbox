/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.river.dropbox.river;

import fr.pilato.elasticsearch.river.dropbox.connector.DropboxAccount;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxChanges;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxConnector;
import fr.pilato.elasticsearch.river.dropbox.connector.DropboxFile;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.Base64;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;

import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @author dadoonet (David Pilato)
 */
public class DropboxRiver extends AbstractRiverComponent implements River {

	private static final DateFormat dropboxDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss ZZZZZ", Locale.US);

	private final Client client;

	private final String indexName;

	private final String typeName;

	private final long bulkSize;

	private volatile Thread feedThread;

	private volatile boolean closed = false;

	private final DropBoxRiverFeedDefinition fsDefinition;

	private final DropboxConnector dropbox;
	
	@SuppressWarnings({ "unchecked" })
	@Inject
	public DropboxRiver(RiverName riverName, RiverSettings settings, Client client)
			throws MalformedURLException {
		super(riverName, settings);
		this.client = client;

		if (settings.settings().containsKey("dropbox")) {
			Map<String, Object> feed = (Map<String, Object>) settings
					.settings().get("dropbox");

			String feedname = XContentMapValues.nodeStringValue(
					feed.get("name"), null);
			String url = XContentMapValues.nodeStringValue(feed.get("url"),
					null);

			int updateRate = XContentMapValues.nodeIntegerValue(
					feed.get("update_rate"), 15 * 60 * 1000);
			
			String[] includes = DropBoxRiverUtil.buildArrayFromSettings(settings.settings(), "dropbox.includes");
			String[] excludes = DropBoxRiverUtil.buildArrayFromSettings(settings.settings(), "dropbox.excludes");

			String appkey = XContentMapValues.nodeStringValue(feed.get("appkey"),	null);
			String appsecret = XContentMapValues.nodeStringValue(feed.get("appsecret"),	null);
			String token = XContentMapValues.nodeStringValue(feed.get("token"),	null);
			String secret = XContentMapValues.nodeStringValue(feed.get("secret"),	null);
			
			fsDefinition = new DropBoxRiverFeedDefinition(feedname, url,
						updateRate, Arrays.asList(includes), Arrays.asList(excludes),
						appkey, appsecret, token, secret);
		} else {
			logger.error(
					"You didn't define the dropbox settings. Exiting... See https://github.com/dadoonet/dropboxriver");
			indexName = null;
			typeName = null;
			bulkSize = 100;
			fsDefinition = null;
			dropbox = null;
			return;
		}

		if (settings.settings().containsKey("index")) {
			Map<String, Object> indexSettings = (Map<String, Object>) settings
					.settings().get("index");
			indexName = XContentMapValues.nodeStringValue(
					indexSettings.get("index"), riverName.name());
			typeName = XContentMapValues.nodeStringValue(
					indexSettings.get("type"), DropBoxRiverUtil.INDEX_TYPE_DOC);
			bulkSize = XContentMapValues.nodeLongValue(
					indexSettings.get("bulk_size"), 100);
		} else {
			indexName = riverName.name();
			typeName = DropBoxRiverUtil.INDEX_TYPE_DOC;
			bulkSize = 100;
		}
		
		// We need to connect to DropBox
		dropbox = new DropboxConnector(fsDefinition.getAppkey(), fsDefinition.getAppsecret(),
				fsDefinition.getToken(), fsDefinition.getSecret());
		DropboxAccount account = dropbox.getUserInfo();
		
		if (account == null) {
			logger.error("Can not connect to your dropbox account. Please check your settings and ensure that " +
					"your app {} is authorized to access to your dropbox account.", fsDefinition.getAppkey());
		} else {
			logger.info("Starting river on {} dropbox account. Scanning {}.", account.email, fsDefinition.getUrl());
		}
		
	}

	@Override
	public void start() {
		if (logger.isInfoEnabled())
			logger.info("Starting dropbox river scanning");
		try {
			client.admin().indices().prepareCreate(indexName).execute()
					.actionGet();
		} catch (Exception e) {
			if (ExceptionsHelper.unwrapCause(e) instanceof IndexAlreadyExistsException) {
				// that's fine
			} else if (ExceptionsHelper.unwrapCause(e) instanceof ClusterBlockException) {
				// ok, not recovered yet..., lets start indexing and hope we
				// recover by the first bulk
				// TODO: a smarter logic can be to register for cluster event
				// listener here, and only start sampling when the block is
				// removed...
			} else {
				logger.warn("failed to create index [{}], disabling river...",
						e, indexName);
				return;
			}
		}
		
		try {
			// If needed, we create the new mapping for files
			pushMapping(indexName, typeName, DropBoxRiverUtil.buildFsFileMapping(typeName));			
		} catch (Exception e) {
			logger.warn("failed to create mapping for [{}/{}], disabling river...",
					e, indexName, typeName);
			return;
		}

		// We create as many Threads as there are feeds
		feedThread = EsExecutors.daemonThreadFactory(
				settings.globalSettings(), "fs_slurper")
				.newThread(
						new DropBoxParser());
		feedThread.start();
	}

	@Override
	public void close() {
		if (logger.isInfoEnabled())
			logger.info("Closing fs river");
		closed = true;

		// We have to close the Thread
		if (feedThread != null) {
			feedThread.interrupt();
		}
	}

    /**
	 * Check if a mapping already exists in an index
	 * @param index Index name
	 * @param type Mapping name
	 * @return true if mapping exists
	 */
	private boolean isMappingExist(String index, String type) {
		ClusterState cs = client.admin().cluster().prepareState().setFilterIndices(index).execute().actionGet().getState();
		IndexMetaData imd = cs.getMetaData().index(index);
		
		if (imd == null) return false;

		MappingMetaData mdd = imd.mapping(type);

		if (mdd != null) return true;
		return false;
	}

	private void pushMapping(String index, String type, XContentBuilder xcontent) throws Exception {
		if (logger.isTraceEnabled()) logger.trace("pushMapping("+index+","+type+")");
		
		// If type does not exist, we create it
		boolean mappingExist = isMappingExist(index, type);
		if (!mappingExist) {
			logger.debug("Mapping ["+index+"]/["+type+"] doesn't exist. Creating it.");

			// Read the mapping json file if exists and use it
			if (xcontent != null) {
				if (logger.isTraceEnabled()) logger.trace("Mapping for ["+index+"]/["+type+"]="+xcontent.string());
				// Create type and mapping
				PutMappingResponse response = client.admin().indices()
					.preparePutMapping(index)
					.setType(type)
					.setSource(xcontent)
					.execute().actionGet();			
				if (!response.isAcknowledged()) {
					throw new Exception("Could not define mapping for type ["+index+"]/["+type+"].");
				} else {
					if (logger.isDebugEnabled()) {
						if (mappingExist) {
							logger.debug("Mapping definition for ["+index+"]/["+type+"] succesfully merged.");
						} else {
							logger.debug("Mapping definition for ["+index+"]/["+type+"] succesfully created.");
						}
					}
				}
			} else {
				if (logger.isDebugEnabled()) logger.debug("No mapping definition for ["+index+"]/["+type+"]. Ignoring.");
			}
		} else {
			if (logger.isDebugEnabled()) logger.debug("Mapping ["+index+"]/["+type+"] already exists and mergeMapping is not set.");
		}
		if (logger.isTraceEnabled()) logger.trace("/pushMapping("+index+","+type+")");
	}

	
	
	
	private class DropBoxParser implements Runnable {
		
		private BulkRequestBuilder bulk;
		private ScanStatistic stats;

		public DropBoxParser() {
			if (logger.isInfoEnabled())
				logger.info("creating dropbox river [{}] for [{}] every [{}] ms",
						fsDefinition.getFeedname(), fsDefinition.getUrl(), fsDefinition.getUpdateRate());
		}

		@Override
		public void run() {
			while (true) {
				if (closed) {
					return;
				}

				try {
					stats = new ScanStatistic(fsDefinition.getUrl());

					String rootPathId = SignTool.sign(fsDefinition.getUrl());
					stats.setRootPathId(rootPathId);

					bulk = client.prepareBulk();

					String lastupdateField = "_cursor";
					String cursor = getCursorFromRiver(lastupdateField);

					cursor = scan(fsDefinition.getUrl(), cursor);

					updateFsRiver(lastupdateField, cursor);

					// If some bulkActions remains, we should commit them
					commitBulk();

				} catch (Exception e) {
					logger.warn("Error while indexing content from {}", fsDefinition.getUrl());
					if (logger.isDebugEnabled())
						logger.debug("Exception for {} is {}", fsDefinition.getUrl(), e);
				}

				try {
					if (logger.isDebugEnabled())
						logger.debug("Fs river is going to sleep for {} ms",
								fsDefinition.getUpdateRate());
					Thread.sleep(fsDefinition.getUpdateRate());
				} catch (InterruptedException e1) {
				}
			}
		}

		@SuppressWarnings("unchecked")
		private String getCursorFromRiver(String lastupdateField) {
			String cursor = null;
			try {
				// Do something
				client.admin().indices().prepareRefresh("_river").execute()
						.actionGet();
				GetResponse lastSeqGetResponse = client
						.prepareGet("_river", riverName().name(),
								lastupdateField).execute().actionGet();
				if (lastSeqGetResponse.isExists()) {
					Map<String, Object> fsState = (Map<String, Object>) lastSeqGetResponse
							.getSourceAsMap().get("dropbox");

					if (fsState != null) {
						Object lastupdate = fsState.get("cursor");
						if (lastupdate != null) {
							cursor = lastupdate.toString();
						}
					}
				} else {
					// First call
					if (logger.isDebugEnabled())
						logger.debug("{} doesn't exist", lastupdateField);
				}
			} catch (Exception e) {
				logger.warn("failed to get _cursor, throttling....", e);
			}

			if (logger.isDebugEnabled())
				logger.debug("cursor: {}", cursor);

			return cursor;
		}

		private void updateFsRiver(String lastupdateField, String cursor)
				throws Exception {

			if (logger.isDebugEnabled())
				logger.debug("updating cursor: {}", cursor);

			// We store the lastupdate date and some stats
			XContentBuilder xb = jsonBuilder()
				.startObject()
					.startObject("dropbox")
						.field("feedname", fsDefinition.getFeedname())
						.field("cursor", cursor)
						.field("docadded", stats.getNbDocScan())
						.field("docdeleted", stats.getNbDocDeleted())
					.endObject()
				.endObject();
			esIndex("_river", riverName.name(), lastupdateField, xb);
		}

		/**
		 * Commit to ES if something is in queue
		 * 
		 * @throws Exception
		 */
		private void commitBulk() throws Exception {
			if (bulk != null && bulk.numberOfActions() > 0) {
				if (logger.isDebugEnabled()) logger.debug("ES Bulk Commit is needed");
				BulkResponse response = bulk.execute().actionGet();
				if (response.hasFailures()) {
					logger.warn("Failed to execute "
							+ response.buildFailureMessage());
				}
			}
		}

		/**
		 * Commit to ES if we have too much in bulk
		 * 
		 * @throws Exception
		 */
		private void commitBulkIfNeeded() throws Exception {
			if (bulk != null && bulk.numberOfActions() > 0 && bulk.numberOfActions() >= bulkSize) {
				if (logger.isDebugEnabled()) logger.debug("ES Bulk Commit is needed");
				
				BulkResponse response = bulk.execute().actionGet();
				if (response.hasFailures()) {
					logger.warn("Failed to execute "
							+ response.buildFailureMessage());
				}
				
				// Reinit a new bulk
				bulk = client.prepareBulk();
			}
		}

		private String scan(String path, String cursor) throws Exception {

			DropboxChanges changes = dropbox.getDelta(cursor);

			for (DropboxFile dropboxFile : changes.getFiles()) {
				if (dropboxFile.getFilename().startsWith(path)) {
					if (dropboxFile.getMeta() != null
							&& !dropboxFile.getMeta().isDir) {
						if (dropboxFile.getMeta().isDeleted) {
							if (DropBoxRiverUtil.isIndexable(
									dropboxFile.getFilename(),
									fsDefinition.getIncludes(),
									fsDefinition.getExcludes())) {
								esDelete(indexName, typeName,
										SignTool.sign(dropboxFile.getFilename()));
								stats.removeFile();
							}
						} else {
							if (DropBoxRiverUtil.isIndexable(
									dropboxFile.getFilename(),
									fsDefinition.getIncludes(),
									fsDefinition.getExcludes())) {
								indexFile(stats, dropboxFile);
								stats.addFile();
							}
						}
					}					
				}
			}

			return changes.getCursor();
		}

		/**
		 * Index a file
		 * 
		 * @param stats
		 * @param dropboxFile
		 * @throws Exception
		 */
		private void indexFile(ScanStatistic stats, DropboxFile dropboxFile) throws Exception {
			if (logger.isDebugEnabled()) logger.debug("Trying to index " + dropboxFile.getFilename());
			
			try {
				byte[] file = dropbox.getFiles("dropbox", dropboxFile.getFilename());
				
				esIndex(indexName,
						typeName,
						SignTool.sign(dropboxFile.getFilename()),
						jsonBuilder()
								.startObject()
								.field(DropBoxRiverUtil.DOC_FIELD_NAME, dropboxFile.getFilename())
								.field(DropBoxRiverUtil.DOC_FIELD_DATE,	convertToEsDate(dropboxFile.getMeta().modified))
								.field(DropBoxRiverUtil.DOC_FIELD_PATH_ENCODED,	SignTool.sign(dropboxFile.getMeta().path))
								.field(DropBoxRiverUtil.DOC_FIELD_ROOT_PATH, stats.getRootPathId())
								.startObject("file").field("_name", dropboxFile.getFilename())
								.field("content", Base64.encodeBytes(file))
								.endObject().endObject());
				
				if (logger.isDebugEnabled()) logger.debug("Index " + dropboxFile.getFilename() + " : success");
				if (logger.isTraceEnabled()) logger.trace("   - " + dropboxFile.getFilename() + " was " + file.length + " bytes");

			} catch (Exception e) {
				logger.warn("Can not index " + dropboxFile.getFilename() + " : " + e.getMessage());
			}

		}
		
		private Long convertToEsDate(String date) {
			try {
				Date dbdate = dropboxDateFormat.parse(date);
				return dbdate.getTime();
			} catch (ParseException e) {
				logger.warn("Can not parse date {}. Returning null.", date);
			}
			return null;
		}

		/**
		 * Add to bulk an IndexRequest
		 * 
		 * @param index
		 * @param type
		 * @param id
		 * @param xb
		 * @throws Exception 
		 */
		private void esIndex(String index, String type, String id,
				XContentBuilder xb) throws Exception {
			if (logger.isDebugEnabled()) logger.debug("Indexing in ES " + index + ", " + type + ", " + id);
			if (logger.isTraceEnabled()) logger.trace("JSon indexed : {}", xb.string());
			
			bulk.add(client.prepareIndex(index, type, id).setSource(xb));
			commitBulkIfNeeded();
		}

		/**
		 * Add to bulk a DeleteRequest
		 * 
		 * @param index
		 * @param type
		 * @param id
		 * @throws Exception 
		 */
		private void esDelete(String index, String type, String id) throws Exception {
			if (logger.isDebugEnabled()) logger.debug("Deleting from ES " + index + ", " + type + ", " + id);
			bulk.add(client.prepareDelete(index, type, id));
			commitBulkIfNeeded();
		}
	}
	
	
	
}
