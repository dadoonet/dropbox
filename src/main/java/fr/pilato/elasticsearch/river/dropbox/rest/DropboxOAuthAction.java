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
import org.scribe.model.Token;

import fr.pilato.elasticsearch.river.dropbox.connector.DropboxConnector;

public class DropboxOAuthAction extends DropboxAction {

	@Inject public DropboxOAuthAction(Settings settings, Client client, RestController controller) {
		super(settings, client);

		// Define Dropbox REST Endpoints
		controller.registerHandler(Method.GET, "/_dropbox/oauth/{appkey}/{appsecret}", this);
		controller.registerHandler(Method.GET, "/_dropbox/oauth/{appkey}/{appsecret}/{oauth_secret}/", this);
	}

	@Override
	public void handleRequest(RestRequest request, RestChannel channel) {
		if (logger.isDebugEnabled()) logger.debug("REST DropboxAction called");

		String appkey = request.param("appkey");
		String appsecret = request.param("appsecret");
		String callback = request.uri();
		String oauth_token = request.param("oauth_token");
		String oauth_secret = request.param("oauth_secret");
		
		if (appkey == null || appsecret == null) {
			onFailure(channel, request, new IOException("appkey and appsecret can not be null."));
		}
		
		try {
			XContentBuilder builder = RestXContentBuilder.restContentBuilder(request);
			if (oauth_token != null && oauth_secret != null) {
				// We are coming back from DropBox
				DropboxConnector dbConn = new DropboxConnector(appkey, appsecret, new Token(oauth_token, oauth_secret));
				dbConn.computeAccessToken();
				Token access = dbConn.getAccessToken();
				
				builder
					.startObject()
						.field(new XContentBuilderString("token"), access.getToken())
						.field(new XContentBuilderString("secret"), access.getSecret())
					.endObject();
			} else {
				// It's the first call. We build the Auth URL
				DropboxConnector dbConn = new DropboxConnector(appkey, appsecret);
				String authUrl = dbConn.getAuthUrl() + "&oauth_callback=http://" + request.header("host") + callback + "/" + dbConn.getRequestToken().getSecret() + "/";
				
				builder
					.startObject()
						.field(new XContentBuilderString("url"), authUrl)
					.endObject();
			}
			channel.sendResponse(new XContentRestResponse(request, RestStatus.OK, builder));
		} catch (IOException e) {
			onFailure(channel, request, e);
		}
	}
}
