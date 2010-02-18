package com.googlecode.csse460_2010.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
	private enum PlayerStates {idle, fighting, eog};
	private PlayerStates state=PlayerStates.idle;
	private PrintWriter out;
	private BufferedReader in;
	private String inputLn, outputLn;

	public Client(Socket socket) {
		super("StirlingZygote");
		this.socket = socket;
	}

	public void run() {

		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));

			Stirling.addPlayer(new Player("test", 1000));
			
			while ((inputLn = in.readLine()) != null){
				
			}
			out.close();
			in.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}