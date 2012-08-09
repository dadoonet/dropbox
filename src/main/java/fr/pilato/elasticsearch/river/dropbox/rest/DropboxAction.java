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
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentThrowableRestResponse;

public abstract class DropboxAction extends BaseRestHandler {

	protected DropboxAction(Settings settings, Client client) {
		super(settings, client);
	}

	protected void onFailure(RestChannel channel, RestRequest request,
			IOException e) {
		try {
			channel.sendResponse(new XContentThrowableRestResponse(request, e));
		} catch (IOException e1) {
			logger.error("Failed to send failure response", e1);
		}
	}
	

}
