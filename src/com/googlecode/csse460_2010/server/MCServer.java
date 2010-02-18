package com.googlecode.csse460_2010.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class MCServer {
	public static void startServer(int port) throws IOException {
        ServerSocket serverSocket = null;
        boolean listening = true;

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
}
/**
 * The Client class is in the MCServer class file as it is only called by the MCServer class.
 * @author Christopher Rabotin
 *
 */
class Client extends Thread{
	private Socket socket = null;

	public Client(Socket socket) {
		super("StirlingZygote");
		this.socket = socket;
	}

	public void run() {

		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));

			Player p = new Player("test", 1000);
			
			out.close();
			in.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}