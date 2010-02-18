package com.googlecode.csse460_2010.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

public class MCServer {
	private static boolean listening = true;
	public static void startServer(int port) throws IOException {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 4444.");
			System.exit(-1);
		}

		while (listening)
			new Client(serverSocket.accept()).start();

		serverSocket.close();
	}
	public static void stopServer(){
		listening = false;
	}
}