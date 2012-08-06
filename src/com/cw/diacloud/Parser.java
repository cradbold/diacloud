package com.cw.diacloud;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class Parser {
	
	private static String DESCRIPTORS_PATH = "descriptors";
	private static String DESCRIPTORS_SUFFIX = ".descriptor";
	
	public static JSONObject getProviders() throws Exception {
		JSONObject providers = new JSONObject();
		JSONArray providersArray = new JSONArray();
	    for (File file : new File(DESCRIPTORS_PATH).listFiles()) {
	        if (file.isFile() && file.getName().endsWith(DESCRIPTORS_SUFFIX)) {
	        	JSONObject provider = new JSONObject();
	        	provider.put("name", Parser.parseProviderName(file));
	            providersArray.add(provider);
	        }
	    }
		providers.put("providers", providersArray);
		return providers;
    }

	private static String parseProviderName(File file) throws Exception {
		JSONObject json = (JSONObject) JSONSerializer.toJSON(IOUtils.toString(new FileReader(file)));
		return json.getJSONObject("provider").getString("name");
	}
	
	public static void main(String[] args) {
		try {
			System.out.println(Parser.getProviders().toString(2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
