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
	private String inputLn, outputLn, tmparg;
	private String[] cmdargs;
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

			me = new Player("test", Stirling.getNoPlayers()+1, 1000);
			Stirling.addPlayer(this); // we "register" our selves to the game engine
			
			// say hello to the new player
			out.println(XMLParser.getWelcomeMsg("@", me));

			while ((inputLn = in.readLine().trim()) != null) {
				// if there are messages pending to be sent (e.g. Multicast
				// msg), we send them now!
				while (msgQ.size() > 0) {
					out.println(msgQ.pop());
				}
				// now we read an understand the information sent from the client
				if(inputLn.startsWith("go")){
					if(me.getState() != Player.States.IDLE){
						outputLn="nomove";
					}else{
						cmdargs = inputLn.split(" ");
						tmparg = cmdargs[1]; // that's the direction
						if(tmparg.equals("up")){
							
						}else if(tmparg.equals("down")){
							
						}else if(tmparg.equals("left")){
							
						}else if(tmparg.equals("right")){
							
						}// we don't treat the unknown direction since it's the client who sends the information
					}
				}else if(inputLn.startsWith("show")){
					
				}else if(inputLn.startsWith("info")){
					
				}else if(inputLn.startsWith("help")){
					
				}else if(inputLn.startsWith("bye")){
					
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