package org.elasticsearch.river.dropbox.itest;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

public class DropboxRiverTest extends AbstractDropboxRiverSimpleTest {

	@Override
	public long waitingTime() throws Exception {
		return 5000;
	}
	
	/**
	 * We use the default mapping
	 */
	@Override
	public XContentBuilder mapping() throws Exception {
		return null;
	}

	/**
	 * 
	 * <ul>
	 *   <li>TODO Fill the use case
	 * </ul>
	 */
	@Override
	public XContentBuilder dropboxRiver() throws Exception {
		// We update every 30 seconds
		int updateRate = 30 * 1000;
		String url = "/test-es";
		
		XContentBuilder xb = jsonBuilder()
				.startObject()
					.field("type", "dropbox")
					.startObject("dropbox")
						.field("appkey", appkey)
						.field("appsecret", appsecret)
						.field("token", token)
						.field("secret", secret)
						.field("url", url)
						.field("update_rate", updateRate)
					.endObject()
				.endObject();
		return xb;
	}
	

	@Test
	public void index_is_not_empty() throws Exception {
		countTestHelper();
	}
}
