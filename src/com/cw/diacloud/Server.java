package com.cw.diacloud;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetSocketAddress;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.io.IOUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server {
	
	public static final int PORT = 7777;

	public static class ProvidersHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			try {
				StringWriter writer = new StringWriter();
				IOUtils.copy(t.getRequestBody(), writer, "UTF-8");
				Server.logRequest(t, writer.toString());
				String response = Parser.getProviders().toString();
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
				Server.logResponse(t, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class InstancesHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			try {
				StringWriter writer = new StringWriter();
				IOUtils.copy(t.getRequestBody(), writer, "UTF-8");
				Server.logRequest(t, writer.toString());
				JSONObject json = (JSONObject) JSONSerializer.toJSON(writer.toString());
				String response = Requester.getInstances(json.getJSONObject("provider").getString("name"));
				t.sendResponseHeaders(200, response.length());
				OutputStream os = t.getResponseBody();
				os.write(response.getBytes());
				os.close();
				Server.logResponse(t, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void logRequest(HttpExchange t, String body) throws IOException {
		System.out.println("REQUEST: " + t.getRequestMethod() + " " + t.getLocalAddress() + t.getRequestURI());
		System.out.println(" -BODY: " + body);
	}
	
	private static void logResponse(HttpExchange t, String body) throws IOException {
		System.out.println("RESPONSE: " + t.getResponseCode());
		System.out.println(" -BODY: " + body);
	}
	
	public static void main(String[] args) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(Server.PORT), 0);
			server.createContext("/providers", new ProvidersHandler());
			server.createContext("/instances", new InstancesHandler());
			server.setExecutor(null);
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
