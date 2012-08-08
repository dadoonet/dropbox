package fr.pilato.elasticsearch.river.dropbox.river;

import java.util.ArrayList;
import java.util.List;

/**
 * Define a DropBox Feed with toke, secret, source (aka short name), url and updateRate attributes
 * @author dadoonet (David Pilato)
 */
public class DropBoxRiverFeedDefinition {
	private String feedname;
	private String url;
	private int updateRate;
	private List<String> includes;
	private List<String> excludes;
	private String appkey;
	private String appsecret;
	private String token;
	private String secret;
	
	
	public DropBoxRiverFeedDefinition() {
		this(null, null, 0, new ArrayList<String>(), new ArrayList<String>(), null, null, null, null);
	}
	
	public DropBoxRiverFeedDefinition(String feedname, String url, int updateRate, 
			String appkey, String appsecret, String token, String secret) {
		this(feedname, url, updateRate, new ArrayList<String>(), new ArrayList<String>(),
				appkey, appsecret, token, secret);
	}
	
	public DropBoxRiverFeedDefinition(String feedname, String url, int updateRate, 
			List<String> includes, List<String> excludes, 
			String appkey, String appsecret, String token, String secret) {
		assert( excludes != null);
		assert( includes != null);
		this.includes = includes;
		this.excludes = excludes;
		this.feedname = feedname;
		this.url = url;
		this.updateRate = updateRate;
		this.appkey = appkey;
		this.appsecret = appsecret;
		this.token = token;
		this.secret = secret;
	}
	
	public String getFeedname() {
		return feedname;
	}
	
	public void setFeedname(String feedname) {
		this.feedname = feedname;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getUpdateRate() {
		return updateRate;
	}

	public void setUpdateRate(int updateRate) {
		this.updateRate = updateRate;
	}
	
	public List<String> getExcludes() {
		return excludes;
	}
	
	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}
	
	public List<String> getIncludes() {
		return includes;
	}
	
	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}
	
	public void addInclude(String include) {
		this.includes.add(include);
	}

	public void addExclude(String exclude) {
		this.excludes.add(exclude);
	}
	
	public String getSecret() {
		return secret;
	}
	
	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getAppkey() {
		return appkey;
	}
	
	public void setAppkey(String appkey) {
		this.appkey = appkey;
	}
	
	public String getAppsecret() {
		return appsecret;
	}
	
	public void setAppsecret(String appsecret) {
		this.appsecret = appsecret;
	}
}
