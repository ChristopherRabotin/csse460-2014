package com.googlecode.csse460_2010.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
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
	private String inputLn, outputLn, tmparg;
	private String[] cmdargs;
	private Player me;
	private Stack<String> msgQ;

	public Client(Socket socket) {
		super("StirlingZygote#"+Stirling.getNoPlayers());
		this.socket = socket;
		msgQ = new Stack<String>();
	}

	public void run() {
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));

			me = new Player(in.readLine().trim(), Stirling.getNoPlayers() + 1,
					1000);
			Stirling.addPlayer(this); // we "register" our selves to the game
			// engine

			// say hello to the new player
			out.println(XMLParser.getWelcomeMsg("@", me));

			while ((inputLn = in.readLine().trim()) != null) {
				// if there are messages pending to be sent (e.g. Multicast
				// msg), we send them now!
				while (msgQ.size() > 0) {
					out.println(msgQ.peek());
				}
				// now we read an understand the information sent from the
				// client
				if (inputLn.startsWith("bye")) {
					killClient();
				} else if(inputLn.startsWith("ping")){
					// enables to see multicast messages quickly
					out.println("pong");
				}else {
					cmdargs = inputLn.split(" ");
					tmparg = cmdargs[1]; // that's the subcommand
					if (inputLn.startsWith("go")) {
						if (me.getState() != Player.States.IDLE) {
							outputLn = "nomove";
						} else {
							try {
								Room newRoom = me.getRoom().getExit(
										Room.stringToDirection(tmparg));
								me.setRoom(newRoom);
							} catch (NullPointerException e) {
								outputLn = "noexit " + tmparg;
							}
						}
					} else if (inputLn.startsWith("show")) {
						if (tmparg.equals("players")) {
							outputLn = "raw\n";
							outputLn += Stirling.getPlayersFormatted();
						} else if (tmparg.equals("attacks")) {
							outputLn = "raw\n";
							outputLn += "Name\t\tDamage\t\tDaemon reserved\n\n";
							HashMap<String, Attack> atks = XMLParser
									.getAttacks();
							Attack atk;
							for (String a : atks.keySet()) {
								atk = atks.get(a);
								outputLn += atk.getName() + "\t\t"
										+ atk.getDamage() + "\t\t"
										+ atk.isDaemonReserved()+"\n";
							}

						} else if (tmparg.equals("points")) {
							outputLn = "points " + me.getPoints();
						} else if (tmparg.equals("daemons")) {
							outputLn = "raw\n";
							outputLn += "Name\t\tValue\t\tHealth\t\tAttacks\n";
							HashMap<String, Daemon> dae = XMLParser
									.getDaemons();
							Daemon tmp;
							for (String dn : dae.keySet()) {
								tmp = dae.get(dn);
								outputLn += tmp.getName() + "\t\t"
										+ tmp.getVictoryPoints() + "\t\t"
										+ tmp.getHealth() + "/"
										+ tmp.getFullHealth() + "\t\t";
								for (Attack atk : tmp.getAttacks()) {
									outputLn += atk.getName() + " ";
								}
								outputLn +="\n";
							}
						} else if (tmparg.equals("exits")) {
							outputLn = "raw\n";
							outputLn += me.getRoom().getExitsFormatted();
						}
					}
				}
				out.println(outputLn);
				outputLn = ""; // reset the value
			}
			killClient();

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