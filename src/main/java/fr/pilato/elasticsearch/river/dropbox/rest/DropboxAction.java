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
