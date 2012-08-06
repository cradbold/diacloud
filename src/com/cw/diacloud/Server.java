package com.cw.diacloud;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Server implements HttpHandler {

	public void handle(HttpExchange t) throws IOException {
		t.getRequestMethod();
		InputStream is = t.getRequestBody();
		//read(is); // .. read the request body
		String response = "Hello World!";
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	public static void main(String[] args) {
		HttpServer server;
		try {
			server = HttpServer.create(new InetSocketAddress(7777), 0);
			server.createContext("/helloworld", new Server());
			server.setExecutor(null);
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
