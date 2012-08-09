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

package fr.pilato.elasticsearch.river.dropbox.rest;

import java.io.IOException;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestRequest.Method;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.action.support.RestXContentBuilder;

public class DropboxHelpAction extends DropboxAction {

	@Inject public DropboxHelpAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		// Define Dropbox REST Endpoints
		controller.registerHandler(Method.GET, "/_dropbox/", this);
	}

	@Override
	public void handleRequest(RestRequest request, RestChannel channel) {
		if (logger.isDebugEnabled()) logger.debug("REST DropboxAction called");

		try {
			XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
			builder
				.startObject()
					.startArray("usage")
						.startObject()
							.field(new XContentBuilderString("method"), "GET")
							.field(new XContentBuilderString("endpoint"), "/_dropbox/")
							.field(new XContentBuilderString("comment"), "This help")
						.endObject()
						.startObject()
							.field(new XContentBuilderString("method"), "GET")
							.field(new XContentBuilderString("endpoint"), "/_dropbox/oauth1/{appkey}/{appsecret}")
							.field(new XContentBuilderString("comment"), "Return the OAuth url to display to user")
						.endObject()
						.startObject()
							.field(new XContentBuilderString("method"), "GET")
							.field(new XContentBuilderString("endpoint"), "/_dropbox/oauth2/{requesttoken}/{requestsecret}/{appkey}/{appsecret}")
							.field(new XContentBuilderString("comment"), "Return the OAuth token/secret for user")
						.endObject()
					.endArray()
				.endObject();
			channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
		} catch (IOException e) {
			onFailure(channel, request, e);
		}
		
	}
}
