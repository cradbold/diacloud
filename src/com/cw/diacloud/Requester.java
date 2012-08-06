package com.cw.diacloud;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;


public class Requester {
	
	public static String getInstances(String providerName) throws Exception {
		String instances = null;
		HttpClient httpclient = new DefaultHttpClient();
    	String requestUrl = "https://identity.api.rackspacecloud.com/v2.0/tokens";
        HttpPost httppost = new HttpPost(requestUrl);
        httppost.addHeader("Content-Type", "application/json");
        String body = "{\"auth\":{\"RAX-KSKEY:apiKeyCredentials\":{\"username\":\"cradbold\", \"apiKey\":\"d799ea61b7a28e9526e7c216e74e105b\"}}}";
        httppost.setEntity(new StringEntity(body));
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		String id = null;
		if (entity != null) {
			String jsonString = IOUtils.toString(entity.getContent());
			JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonString);        
	        JSONObject token = json.getJSONObject("access").getJSONObject("token");
	        id = token.getString("id");
			httppost.abort();
		}
		httpclient.getConnectionManager().shutdown();
		httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("https://dfw.servers.api.rackspacecloud.com/v2/713310/servers/detail");
		httpget.addHeader("X-Auth-Token", id);
		response = httpclient.execute(httpget);
		entity = response.getEntity();
		if (entity != null) {
			instances = IOUtils.toString(entity.getContent());
			httpget.abort();
		}
		httpclient.getConnectionManager().shutdown();
		return instances;
    }
}
