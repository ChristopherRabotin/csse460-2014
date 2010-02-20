package com.googlecode.csse460_2010.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Stack;

/**
 * The Client class is in the MCServer class file as it is only called by the
 * MCServer class.
 * 
 * @author Christopher Rabotin
 * 
 */
public class Client extends Thread {
	private Socket socket = null;
	private PrintWriter out;
	private BufferedReader in;
	private String inputLn, outputLn;
	private Player me;
	private Stack<String> msgQ;

	public Client(Socket socket) {
		super("StirlingZygote");
		this.socket = socket;
		msgQ = new Stack<String>();
	}

	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));

			me = new Player("test", 1000,Stirling.getNoPlayers()+1);
			Stirling.addPlayer(this); // we "register" our selves to the game engine
			
			// say hello to the new player
			out.println(XMLParser.getWelcomeMsg("@", me));

			while ((inputLn = in.readLine()) != null) {
				// if there are messages pending to be sent (e.g. Multicast
				// msg), we send them now!
				while (msgQ.size() > 0) {
					out.println(msgQ.pop());
				}
				

			}
			killClient();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void queueMsg(String msg) {
		msgQ.push(msg);
	}
	
	public void killClient(){
		Stirling.rmPlayer(this);
		out.close();
		try {
			in.close();
			socket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}