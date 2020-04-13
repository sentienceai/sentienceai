package com.airobotics.robot.util;

import java.io.IOException;

public interface IHttpClient {
	public void init(String host, int port) throws IOException;
	public String get(String host, int port, String path) throws IOException;
	public String post(String host, int port, String path, String data) throws IOException;
}
