package com.cw.diacloud;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.commons.io.IOUtils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;


public class Parser {
	
	private static final String ENDPOINT = "ec2.us-west-1.amazonaws.com".toLowerCase();
	private static final String AWS_ACCESS_KEY_ID = "AKIAJY7XBVQE3HJY6PZQ";
    private static final String AWS_SECRET_KEY = "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";
    private static final String UTF8_CHARSET = "UTF-8";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String REQUEST_METHOD = "GET";
    
    private static final String USERNAME = "cradbold";
    private static final String API_KEY = "d799ea61b7a28e9526e7c216e74e105b";
    private static final String ACCOUNT_ID = "713310";
    private static final String URL = "https://dfw.servers.api.rackspacecloud.com/v2/" + ACCOUNT_ID + "/servers/detail";
	
    private static void awsDescribeInstances() throws Exception {
    	HttpClient httpclient = new DefaultHttpClient();
    	byte[] secretyKeyBytes = AWS_SECRET_KEY.getBytes(UTF8_CHARSET);
    	SecretKeySpec secretKeySpec = new SecretKeySpec(secretyKeyBytes, HMAC_SHA256_ALGORITHM);
    	Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
    	mac.init(secretKeySpec);
        String requestUrl = null;
		Map<String, String> params = new HashMap<String, String>();
		params.put("Action", "DescribeInstances");
		params.put("Version", "2012-07-20");
		params.put("SignatureVersion", "2");
		params.put("SignatureMethod", "HmacSHA256");
		params.put("AWSAccessKeyId", AWS_ACCESS_KEY_ID);
        params.put("Timestamp", Parser.timestamp());
        SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(params);
        String canonicalQS = Parser.canonicalize(sortedParamMap);
        String toSign = REQUEST_METHOD + "\n" 
            + ENDPOINT + "\n"
            + "/\n"
            + canonicalQS;
        String hmac = Parser.hmac(toSign, mac);
        String sig = Parser.percentEncodeRfc3986(hmac);
        requestUrl = "https://" + ENDPOINT + "/?" + canonicalQS + "&Signature=" + sig;
//        System.out.println("Request is \"" + requestUrl + "\"");
        HttpGet httpget = new HttpGet(requestUrl);
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
    }
    
    private static void rackspaceDescribeInstances() throws Exception {
    	HttpClient httpclient = new DefaultHttpClient();
    	String requestUrl = "https://identity.api.rackspacecloud.com/v2.0/tokens";
        HttpPost httppost = new HttpPost(requestUrl);
        httppost.addHeader("Content-Type", "application/json");
        String body = "{\"auth\":{\"RAX-KSKEY:apiKeyCredentials\":{\"username\":\"" + USERNAME + "\", \"apiKey\":\"" + API_KEY + "\"}}}";
        httppost.setEntity(new StringEntity(body));
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		String id = null;
		if (entity != null) {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
//			String line = reader.readLine();
//			while (line != null) {
//				System.out.println(line);
//				line = reader.readLine();
//			}
			String jsonString = IOUtils.toString(entity.getContent());
			JSONObject json = (JSONObject) JSONSerializer.toJSON(jsonString);        
	        JSONObject token = json.getJSONObject("access").getJSONObject("token");
	        id = token.getString("id");
//	        System.out.println(json.toString(4));
//	        System.out.println("token:" + id);
			httppost.abort();
		}
		httpclient.getConnectionManager().shutdown();
		httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(URL);
		httpget.addHeader("X-Auth-Token", id);
//		System.out.println("get request:" + httpget.toString());
		response = httpclient.execute(httpget);
		entity = response.getEntity();
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
			Parser.awsDescribeInstances();
			Parser.rackspaceDescribeInstances();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String hmac(String stringToSign, Mac mac) {
        String signature = null;
        byte[] data;
        byte[] rawHmac;
        try {
            data = stringToSign.getBytes(UTF8_CHARSET);
            rawHmac = mac.doFinal(data);
            Base64 encoder = new Base64();
            signature = new String(encoder.encode(rawHmac));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
        }
        return signature;
    }

    private static String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    private static String canonicalize(SortedMap<String, String> sortedParamMap) {
        if (sortedParamMap.isEmpty()) { return ""; }
        StringBuffer buffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> kvpair = iter.next();
            buffer.append(percentEncodeRfc3986(kvpair.getKey()));
            buffer.append("=");
            buffer.append(percentEncodeRfc3986(kvpair.getValue()));
            if (iter.hasNext()) {
                buffer.append("&");
            }
        }
        String cannoical = buffer.toString();
        return cannoical;
    }

    private static String percentEncodeRfc3986(String s) {
        String out;
        try {
            out = URLEncoder.encode(s, UTF8_CHARSET)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            out = s;
        }
        return out;
    }
}
