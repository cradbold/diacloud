package com.cw.diacloud.test;

import java.util.Iterator;

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

import com.cw.diacloud.Server;


public class Client {
	
	private static final String ENDPOINT = "http://localhost:" + Server.PORT;
	private static final String PROVIDERS_CONTEXT = "/providers";
	private static final String INSTANCES_CONTEXT = "/instances";
	
    private static String getProviders() throws Exception {
    	String providers = null;
    	HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(ENDPOINT + PROVIDERS_CONTEXT);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			providers = IOUtils.toString(entity.getContent());
			httpget.abort();
		}
		httpclient.getConnectionManager().shutdown();
		return providers;
    }
    
    private static String getInstances(String providerName) throws Exception {
    	String instances = null;
    	HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(ENDPOINT + INSTANCES_CONTEXT);
        HttpEntity requestBody = new StringEntity("{\"provider\":{\"name\":\"rackspace\"}}");
        httppost.setEntity(requestBody);
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			instances = IOUtils.toString(entity.getContent());
			httppost.abort();
		}
		httpclient.getConnectionManager().shutdown();
		return instances;
    }
    
    	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			String providers = Client.getProviders();
//			System.out.println("providers:" + providers);
			JSONObject json = (JSONObject) JSONSerializer.toJSON(providers);
			Iterator<JSONObject> iterator = json.getJSONArray("providers").iterator();
//			while (iterator.hasNext()) {
				String providerName = iterator.next().getString("name");
				System.out.println("Provider name:" + providerName);
				String instances = Client.getInstances("rackspace");//providerName);
				System.out.println(" -Instances:" + instances);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
