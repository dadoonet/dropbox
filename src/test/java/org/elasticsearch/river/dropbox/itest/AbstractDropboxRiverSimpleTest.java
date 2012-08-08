package org.elasticsearch.river.dropbox.itest;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Assert;


public abstract class AbstractDropboxRiverSimpleTest extends AbstractDropboxRiverTest {

	/**
	 * We wait for 5 seconds before each test
	 */
	@Override
	public long waitingTime() throws Exception {
		return 5;
	}

	protected void searchTestHelper(String feedname) {
		// Let's search for entries for darkreading
//		SearchResponse searchResponse = node.client().prepareSearch(indexName())
//				.setQuery(QueryBuilders.fieldQuery("feedname", feedname)).execute().actionGet();
//		Assert.assertTrue("We should have at least one doc for " + feedname + "...", searchResponse.hits().getTotalHits() > 1);
	}
	
	public void countTestHelper() throws Exception {
		// Let's search for entries
		CountResponse response = node.client().prepareCount(indexName())
				.setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
		Assert.assertTrue("We should have at least one doc...", response.count() > 1);
	}

}
