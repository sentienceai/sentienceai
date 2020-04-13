package com.airobotics.robot.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class HttpClient implements IHttpClient {
	protected Socket socket;
	protected BufferedWriter writer;
	protected BufferedReader reader;
	
	public void init(String host, int port) throws IOException {
		socket = new Socket(host, port);
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}
	
	public String get(String host, int port, String path) throws IOException {
		return getResponse(host, port, path, null);
	}

	public String post(String host, int port, String path, String data) throws IOException {
		return getResponse(host, port, path, data);
	}

	protected String getResponse(String host, int port, String path, String data) throws IOException {
		init(host, port);
		
		write(path, data, writer);
		List<String> headers = new LinkedList<String>();
		String body = read(headers, reader);
		
		close(socket, writer, reader);

		return body;
	}

	protected void write(String path, String data, BufferedWriter wr) throws IOException {
		if (data == null) {
			wr.write("GET " + path + " HTTP/1.0\r\n\r\n");
		} else {
			wr.write("POST " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + data.length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");
			wr.write(data);
		}
		wr.flush();
	}

	protected String read(List<String> headers, BufferedReader rd) throws IOException {
		String body = "";
		boolean endOfHeader = false;
		String line;
		while ((line = rd.readLine()) != null) {
			if (isEofWithSocketProxy(endOfHeader, line))
				break;
			if (line.isEmpty()) endOfHeader = true;
			if (!endOfHeader) headers.add(line);
			else body = line;
		}
		return body;
	}

	protected boolean isEofWithSocketProxy(boolean endOfHeader, String line) {
		return false;
	}

	protected void close(Socket socket, BufferedWriter writer, BufferedReader reader) throws IOException {
		writer.close();
		reader.close();
		socket.close();
	}
}