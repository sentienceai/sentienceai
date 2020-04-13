package com.airobotics.robot.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class NxtHttpClient extends HttpClient {
	public String getResponse(String host, int port, String path, String data) throws IOException {
		if (socket == null)
			init(host, port);
		
		write(path, data, writer);
		List<String> headers = new LinkedList<String>();
		String body = read(headers, reader);
		
		return body;
	}

	protected boolean isEofWithSocketProxy(boolean endOfHeader, String line) {
		return endOfHeader && line.isEmpty();
	}
}
