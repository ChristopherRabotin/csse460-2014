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
		super("StirlingZygote#" + Stirling.getNoPlayers());
		if (!MCServer.getListening())
			try {
				Stirling.log.info("Server should be dead but isn't!");
				this.finalize();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		this.socket = socket;
		msgQ = new Stack<String>();
		Stirling.log.finest("New client");
	}

	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));

			me = new Player(in.readLine().trim(), Stirling.getNoPlayers() + 1,
					1000);
			me.addAttack(XMLParser.getPlayersDefaultAttack());
			Stirling.addPlayer(this); // we "register" our selves to the game
			// engine

			// say hello to the new player
			out.println(XMLParser.getWelcomeMsg("@", me));
			out.println("room:" + me.getRoom().getName());
			
			try {
				while ((inputLn = in.readLine().trim()) != null) {
					// if there are messages pending to be sent (e.g. Multicast
					// msg), we send them now!
					while (msgQ.size() > 0) {
						out.println(msgQ.peek());
						msgQ.remove(0); // it's a sloppy implementation of a
						// FIFO but it works.
					}
					// now we read an understand the information sent from the
					// client
					if (inputLn.startsWith("bye")) {
						killClient();
					} else if (inputLn.startsWith("ping")) {
						// enables to see multicast messages quickly
						out.println("pong");
					} else if (inputLn.startsWith("godmode")) {
						me.beatify();
						out.println("Roger.");
					} else if (inputLn.startsWith("killserver")) {
						if (me.isBlessed()) {
							out.println("granted"); // we send out the message
							// directly because the
							// server WILL shut down
							Stirling.endGame(this);
						} else {
							outputLn = "denied";
						}
					} else {
						outputLn = Stirling.processClientInput(me, inputLn);
					}
					if (outputLn != null)
						out.println(outputLn);
					outputLn = ""; // reset the value
				}
			} catch (IOException e) {
				killClient();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void queueMsg(String msg) {
		msgQ.push(msg);
	}

	public void killClient() {
		Stirling.rmPlayer(this);
		out.close();
		try {
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Player getPlayer() {
		return me;
	}
}