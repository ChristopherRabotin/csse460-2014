package com.googlecode.csse460_2010.server;

import java.io.IOException;
import java.net.ServerSocket;

public class MCServer {
	private static boolean listening = true; 
	
	public static void startServer() throws IOException {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(XMLParser.getServerPort());
			System.out.println("Starting server");
		} catch (IOException e) {
			System.err.println("Could not listen on port: "+XMLParser.getServerPort()+".");
			System.exit(-1);
		}

		while (listening && Stirling.getNoPlayers() < XMLParser.getServerMaxConn()-1){
			new Client(serverSocket.accept()).start();
		}

		serverSocket.close();
	}
	public static void stopServer(){
		listening = false;
	}
	public static boolean getListening(){
		return listening;
	}
}