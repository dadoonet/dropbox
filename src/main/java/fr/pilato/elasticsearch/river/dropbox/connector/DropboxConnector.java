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

package fr.pilato.elasticsearch.river.dropbox.connector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;


/**
 * DropBox Authentification Helper for OAuth process
 * <br>You must create your own dropbox application on https://www.dropbox.com/developers/apps
 * before using this class.
 * <ul>
 * <li>Step 1 : Get a request Token for a given application
 * <li>Step 2 : Compute the Authorization URL from a given token
 * <li>Step 3 : After user authorization on DropBox site, get an Access Token on APIs
 * </ul>
 * @author David Pilato (aka dadoonet)
 *
 */
public class DropboxConnector {

	private final String appkey;
	private final String appsecret;
	private final Token requestToken;
	private final OAuthService service;
	private final String authUrl;
	private Token accessToken;
	
	/**
	 * Build a DropBox Connector for your APP and start OAuth : Step 1 and 2 :
	 * <ul>
	 * <li>OAuth : Step 1 : Get a request Token for a given application.
	 * <li>OAuth : Step 2 : Compute the Authorization URL from a given token.
	 * </ul>
	 * Your next step should be to redirect your user to {@link #getAuthUrl()} and 
	 * when done, call {@link #computeAccessToken()} to set token and secret
	 * @param appkey Your App Secret
	 * @param appsecret Your App Key
	 * @see #getRequestToken()
	 */
	public DropboxConnector(String appkey, String appsecret) {
		if (appkey == null || appsecret == null) throw new IllegalArgumentException("Your Dropbox appkey and appsecret can not be null");
		
		this.appkey = appkey;
		this.appsecret = appsecret;
		this.service = getAuthService(this.appkey, this.appsecret);
		this.requestToken = this.service.getRequestToken();
		this.authUrl = this.service.getAuthorizationUrl(this.requestToken);	
	}

	/**
	 * Build a DropBox Connector for your APP and start OAuth : Step 1 and 2 :
	 * <ul>
	 * <li>OAuth : Step 1 : Get a request Token for a given application.
	 * <li>OAuth : Step 2 : Compute the Authorization URL from a given token.
	 * </ul>
	 * Your next step should be to redirect your user to {@link #getAuthUrl()} and 
	 * when done, call {@link #computeAccessToken()} to set token and secret
	 * @param appkey Your App Secret
	 * @param appsecret Your App Key
	 * @see #getRequestToken()
	 */
	public DropboxConnector(String appkey, String appsecret, Token requestToken) {
		if (appkey == null || appsecret == null || requestToken == null) throw new IllegalArgumentException("Your Dropbox appkey, appsecret and requestToken can not be null");
		
		this.appkey = appkey;
		this.appsecret = appsecret;
		this.service = getAuthService(this.appkey, this.appsecret);
		this.requestToken = requestToken;
		this.authUrl = null;
	}

	/**
	 * Build a DropBox Connector for your APP with your already known DropBox token and secret
	 * @param appkey Your App Secret
	 * @param appsecret Your App Key
	 * @param token DropBox Access Token
	 * @param secret DropBox Access Secret
	 * @see #getRequestToken()
	 */
	public DropboxConnector(String appkey, String appsecret, String token, String secret) {
		if (appkey == null || appsecret == null) throw new IllegalArgumentException("Your Dropbox appkey and appsecret can not be null");
		if (token == null || secret == null) throw new IllegalArgumentException("Your Dropbox token and secret can not be null");
		
		this.appkey = appkey;
		this.appsecret = appsecret;
		this.service = getAuthService(this.appkey, this.appsecret);
		this.accessToken = new Token(token, secret);
		this.requestToken = null;
		this.authUrl = null;
	}

	/**
	 * OAuth : Step 3 : After user authorization on DropBox site,
	 * get an Access Token on APIs
     * <br/>(see <a href="https://www.dropbox.com/developers/apps">DropBox API</a>)
	 * @return The connector to chain actions
	 */
	public DropboxConnector computeAccessToken() {
		Verifier verifier = new Verifier("verifier you got from the user");
		accessToken = service.getAccessToken(requestToken, verifier);
		return this;
	}

	/**
	 * Get user's account information
     * <br/>(see <a href="https://www.dropbox.com/developers/apps">DropBox API</a>)
	 * @return DropBox Account Details
	 */
	public DropboxAccount getUserInfo() {
		Response response = getResponse(Verb.GET, "https://api.dropbox.com/1/account/info", (PostOption[]) null);
		Map<String, Object> map = XContentHelper.convertToMap(response.getBody().getBytes(), 0, response.getBody().length(), false).v2();
		return new DropboxAccount(map);
	}

	public byte[] getFiles(String root, String path) {
		Response response = getResponse(Verb.GET, "https://api-content.dropbox.com/1/files/"+root+encodePath(path), (PostOption[]) null);
		return getContent(response);
	}

    private byte[] getContent(Response response) {
        ByteArrayOutputStream bos = null;
        long totalRead = 0;
        long length = Long.parseLong(response.getHeader("Content-Length"));
        InputStream is = response.getStream();
        
        try {
            bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int read;
            while (true) {
                read = is.read(buffer);
                if (read < 0) {
                    if (length >= 0 && totalRead < length) {
                        // We've reached the end of the file, but it's unexpected.
                        throw new RuntimeException("Unexpected end of stream : " + totalRead);
                    }
                    // TODO check for partial success, if possible
                    break;
                }

                bos.write(buffer, 0, read);

                totalRead += read;
            }

            bos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            String message = e.getMessage();
            if (message != null && message.startsWith("No space")) {
                // This is a hack, but it seems to be the only way to check
                // which exception it is.
                throw new RuntimeException("Not enough space");
            } else {
                /*
                 * If the output stream was closed, we notify the caller
                 * that only part of the file was copied. This could have
                 * been because this request is being intentionally
                 * canceled.
                 */
                throw new RuntimeException("Outputstream is closed before end of reading");
            }
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {}
            }
            // This will also abort/finish the request if the download is
            // canceled early.
            try {
                is.close();
            } catch (IOException e) {}
        }
    }
    
    
	public static String encodePath(String path) {
		try {
            String target = URLEncoder.encode(path, "UTF-8");
            target = target.replace("%2F", "/");
            target = target.replace("+", "%20").replace("*", "%2A");
			return target;
		} catch (UnsupportedEncodingException e) {
			// TODO Add warn to logger
		}
		return path;
	}
	
	@SuppressWarnings("unchecked")
	public DropboxChanges getDelta(String cursor) {
		DropboxChanges changes = new DropboxChanges();
		
		PostOption option = null;
		boolean hasMore = true;
		
		while (hasMore) {
			if (cursor != null) option = new PostOption("cursor", cursor);
			
			Response response = getResponse(Verb.POST, "https://api.dropbox.com/1/delta", option);
			Map<String, Object> map = XContentHelper.convertToMap(response.getBody().getBytes(), 0, response.getBody().length(), false).v2();
			Object oCursor = XContentMapValues.extractValue("cursor", map);
			Object oHasMore = XContentMapValues.extractValue("has_more", map);
			
			if (oHasMore != null) {
				hasMore = (Boolean) oHasMore;
				 cursor = (String) oCursor;
			} else {
				hasMore = false;
			}
			
			// We get some entries here
			Object oEntries = XContentMapValues.extractValue("entries", map);
			if (oEntries != null && XContentMapValues.isArray(oEntries)) {
				ArrayList<?> entries = (ArrayList<?>) oEntries;
				
				for (Object oEntry : entries) {
					if (XContentMapValues.isArray(oEntry)) {
						ArrayList<?> entry = (ArrayList<?>) oEntry;
						
						// We should have filename in [0] and metadata in [1]
						Object oFilename = entry.get(0);
						Object oFilemeta = null;
						
						if (oFilename instanceof String) {
							oFilemeta = entry.get(1);
						} else {
							oFilename = entry.get(1);
							oFilemeta = entry.get(0);
						}
						Map<String, Object> mapMeta = (Map<String, Object>) oFilemeta;
						String filename = (String) oFilename;
						
						DropboxEntry meta = new DropboxEntry(mapMeta);

						// We check if we already have this file
						DropboxFile dropboxfile = changes.get(filename);
						if (dropboxfile == null) {
							dropboxfile = new DropboxFile(meta, filename);
							changes.add(dropboxfile);
						} else {
							dropboxfile.getMeta().update(mapMeta);
						}
					} 
				}
			}			
		}
		
		changes.setCursor(cursor);
		return changes;
	}

		
	/**
	 * Build the DropBoxApi service
	 * @param appkey Your app key
	 * @param appsecret Your app secret
	 * @return the OAuthService
	 */
	protected static OAuthService getAuthService(String appkey, String appsecret) {
		OAuthService service = new ServiceBuilder()
			.provider(DropBoxApi.class)
			.apiKey(appkey)
			.apiSecret(appsecret)
			.callback("http://localhost:9200/_dropbox/oauth2/")
			// .debug()
			.build();		
		
		return service;
	}

	protected Response getResponse(Verb verb, String url, PostOption... options) {
		OAuthRequest request = new OAuthRequest(verb, url);
		
		if (options != null) {
			for (PostOption postOption : options) {
				if (postOption != null) {
					request.addBodyParameter(postOption.getOption(), postOption.getValue());
				}
			}
		}
		getAuthService(appkey, appsecret).signRequest(accessToken, request);
		Response response = request.send();
		
		if (response.getCode() != 200) {
			throw new RuntimeException("Dropbox HTTP Error " + response.getCode() + " : " + response.getBody());
		}
		
		// TODO Add to log.debug
		// System.out.println(response.getBody());
		
		return response;
	}

	public String getAuthUrl() {
		return authUrl;
	}
	
	public Token getAccessToken() {
		return accessToken;
	}
	
	public Token getRequestToken() {
		return requestToken;
	}
}
