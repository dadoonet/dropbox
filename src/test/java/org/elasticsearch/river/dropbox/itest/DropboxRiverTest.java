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

package org.elasticsearch.river.dropbox.itest;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Test;

public class DropboxRiverTest extends AbstractDropboxRiverSimpleTest {

	@Override
	public long waitingTime() throws Exception {
		return 30;
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
