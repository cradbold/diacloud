package com.cw.diacloud.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;


public class Client {
	
	private static final String ENDPOINT = "http://localhost:7777/helloworld".toLowerCase();
	
    private static void getProviders() throws Exception {
    	HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(ENDPOINT);
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			httpget.abort();
		}
		httpclient.getConnectionManager().shutdown();
    }
    
    	
	public static void main(String[] args) {
		try {
			Client.getProviders();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
