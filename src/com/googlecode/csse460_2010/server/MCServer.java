package com.googlecode.csse460_2010.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This is the multi client server. For every connection it creates a new thread
 * for the client.
 * 
 * @author Christopher Rabotin
 * 
 */
public class MCServer {
	private static boolean listening = true;

	/**
	 * This method starts the multi client server.
	 * 
	 * @throws IOException
	 *             if we cannot close the socket
	 */
	public static void startServer() throws IOException {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(Stirling.getServerPort());
			Stirling.log.info("Starting server");
		} catch (IOException e) {
			Stirling.log.severe("Could not listen on port: "
					+ Stirling.getServerPort() + ".");
			System.exit(-1);
		}

		while (listening
				&& Stirling.getNoPlayers() < XMLParser.getServerMaxConn() - 1) {
			new Client(serverSocket.accept()).start();
		}

		serverSocket.close();
	}

	/**
	 * This method prevents two or more new clients to connect. Since accept()
	 * is a blocking method and already called, one more new client can connect
	 * to the server before it is stopped. However, that exception is taken care
	 * of in the Client.java constructor.
	 */
	public static void stopServer() {
		listening = false;
	}

	/**
	 * Checks whether the server should be running.
	 * 
	 * @return listening
	 */
	public static boolean getListening() {
		return listening;
	}
}