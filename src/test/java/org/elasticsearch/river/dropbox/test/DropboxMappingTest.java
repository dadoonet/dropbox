package org.elasticsearch.river.dropbox.test;


import org.apache.log4j.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

import fr.pilato.elasticsearch.river.dropbox.river.DropBoxRiverUtil;

public class DropboxMappingTest {
	
	private Logger logger = Logger.getLogger(DropboxMappingTest.class);

	@Test
	public void fs_mapping_for_files() throws Exception {
		XContentBuilder xb = DropBoxRiverUtil.buildFsFileMapping();
		logger.debug("Mapping used for files : " + xb.string());
	}
	
	@Test
	public void fs_mapping_for_folders() throws Exception {
		XContentBuilder xb = DropBoxRiverUtil.buildFsFolderMapping();
		logger.debug("Mapping used for folders : " + xb.string());
	}

	@Test
	public void fs_mapping_for_meta() throws Exception {
		XContentBuilder xb = DropBoxRiverUtil.buildFsRiverMapping();
		logger.debug("Mapping used for river metadata : " + xb.string());
	}

}
