package com.googlecode.csse460_2010.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * This is the main class of the Client part of the project. As the
 * documentation specifies it, there are no input arguments necessary.
 * 
 * @author Christopher Rabotin
 * 
 */
public class Client {
	static private int port, timeout;
	static private String host, name, inputLn, outputLn;
	static private InetAddress addr;
	static private SocketAddress sockaddr;
	static private Socket skt;
	static private boolean processRaw = false;
	static private BufferedReader readFromSkt;
	static private PrintWriter writeToSkt;
	/*
	 * These variables will be used in the reflection which is why Java thinks
	 * they may not be used.
	 */
	@SuppressWarnings("unused")
	static private String room, latestDirection, latestAttack;

	/**
	 * This is the main function. No arguments are necessary.
	 * <ul>
	 * <li><b>Initialization:</b> load and parse the configuration file
	 * (clientConf.xml). Then read host, port and timeout configuration.</li>
	 * <li><b>Game startup:</b> print the welcome message and ask for the player
	 * his/her name. Then connect to the host and send it the name of the player
	 * (as defined in the server protocol).</li>
	 * <li><b>Game play:</b> ask for a user input and read output from the
	 * server. See documentation for processClientInput() and
	 * processServerMsg(String).</li>
	 * </ul>
	 * 
	 * @param args
	 *            Unused.
	 */
	public static void main(String[] args) {
		/*
		 * We start by reading the XML file to communicate with the server.
		 */
		try {
			XMLParser.loadNParse("clientConf.xml");
		} catch (Throwable e) {
			System.err.println("Error while loading the XML file!");
			e.printStackTrace();
			System.exit(0);
		}
		port = XMLParser.getCnxPort();
		timeout = XMLParser.getCnxTimeout();
		host = XMLParser.getCnxHost();
		/*
		 * Then we print the welcome message to the user.
		 */
		System.out.println(XMLParser.getClientMsg("welcome"));
		try {
			name = new BufferedReader(new InputStreamReader(System.in))
					.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		System.out.println(XMLParser.parseMsg(
				XMLParser.getClientMsg("nameThx"), XMLParser.class));
		try {
			addr = InetAddress.getByName(host);
			sockaddr = new InetSocketAddress(addr, port);

			skt = new Socket();

			/*
			 * This method will block no more than timeout (in milliseconds). If
			 * the timeout occurs, SocketTimeoutException is thrown.
			 */
			skt.connect(sockaddr, timeout);
			readFromSkt = new BufferedReader(new InputStreamReader(skt
					.getInputStream()));
			writeToSkt = new PrintWriter(skt.getOutputStream(), true);

			writeToSkt.println(name);
			/*
			 * Now we start the ping timer
			 */
			new SendPing();

			while ((inputLn = readFromSkt.readLine()) != null) {
				if (inputLn.length() > 0)
					processServerMsg(inputLn);
				/*
				 * The server may send empty messages so we want to wait for
				 * actual data to arrive to ask for the user new input
				 */
				else
					continue;
				if (!processRaw) {
					do {
						outputLn = processClientInput();
					} while (outputLn == null);
					writeToSkt.println(outputLn);

				}
			}

			readFromSkt.close();
			writeToSkt.close();
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * This function processed the client input. It starts by printing the
	 * message to ask the user for his/her input. After reading the input, the
	 * function extracts the <i>command</i> part and the <i>argument</i> part of
	 * the input. The command part corresponds to the first word (each words are
	 * considered separated by a space). What is called the argument is the
	 * second word. Then the function calls XMLParser to get the corresponding
	 * Command instance. Using reflection (java.lang.reflect) if the reflection
	 * variable of the given Command instance is not null, we store in the
	 * correct variable the <i>argument</i> of the input being processed.
	 * Finally, we call the toServerCmd() method of Command instance.
	 * 
	 * @return the input of the user <i>translated</i> into the server protocol
	 */
	private static String processClientInput() {
		System.out.println(XMLParser.getClientMsg("input"));
		String toServer = null, in, arg, reflectVar;
		Field fd;
		try {
			in = new BufferedReader(new InputStreamReader(System.in))
					.readLine();
			Command c = XMLParser.getCmd(in.split(" ")[0]);
			if (c.requestsArgument()) {
				arg = in.split(" ")[1];
				toServer = c.toServerCmd(arg);
				reflectVar = c.getReflectVar();
				if (reflectVar != null) {
					try {
						fd = Client.class.getDeclaredField(reflectVar);
						fd.setAccessible(true);
						fd.set(Client.class, arg);
					} catch (Throwable e) {
						System.err
								.println("No such field ["
										+ reflectVar
										+ "]. Make sure it is defined in client/Client.java.");
						e.printStackTrace();
						System.exit(0);
					}
				}
			}
		} catch (IllegalArgumentException e) {
			System.out.println(XMLParser.getClientMsg("invalid").replace("@",
					e.getMessage()));
		} catch (IOException e) {
			System.err.println("Unable to read from input at this time!\n" + e);
		}
		return toServer;
	}

	/**
	 * This method processed the information sent by the server to the client.
	 * The information is processed line by line. There are two kinds of data
	 * sent by the server: the <i>raw</i> data and the <i>non-raw</i> data. Data
	 * is considered <i>raw</i> when the first and last line are respectively
	 * <b>raw</b> and <b>endraw</b>. If the data is <i>raw</i> we simply display
	 * it to the player. Otherwise, it is a standard protocol message. Non-raw
	 * data can have multiple commands on the same line. Therefore, we start by
	 * extracting all the commands, which are separated by spaces when coming
	 * from the server. Then we extract the argument and the command name from
	 * each command. They are separated by <i>:</i>. Then the function fetches
	 * the protocol translation (described in the client configuration file) and
	 * replace every occurrence of <i>@</i> by the argument sent by the server.
	 * Finally, the message to be shown to the player is parsed with the
	 * reflection function: it replaces every word starting with a dollar sign
	 * (<i>$</i>) with its variable value in the Client class.
	 * 
	 * @param str
	 *            one line of the data sent by the server.
	 */
	public static void processServerMsg(String str) {
		if (str.startsWith("raw")) {
			/*
			 * we don't print out anything as endraw is on its own line
			 */
			processRaw = true;
		} else if (str.startsWith("endraw")) {
			/*
			 * we don't print out anything as endraw is on its own line
			 */
			processRaw = false;
		} else if (processRaw) {
			/*
			 * if we are processing a raw input then we simply output it, no
			 * questions asked. It is the last of the "raw" treatment otherwise
			 * the "endraw" is never reached.
			 */
			System.out.println(str);
		} else {
			/*
			 * then we look up the meaning in the XMLParser class.
			 */
			String[] cmds = str.split("\\|"), args;
			String toClient = "";
			for (String cmd : cmds) {
				args = cmd.split(":");
				toClient = XMLParser.getServerMsg(args[0]);
				if (args.length > 1) {
					/*
					 * if there is an argument to the message sent by the
					 * server, then replace the server message.
					 */
					toClient = toClient.replaceAll("@", args[1]);
				}
				toClient = XMLParser.parseMsg(toClient, Client.class);
				System.out.println(toClient);
			}
		}
	}

	public static PrintWriter getWriteToSkt() {
		return writeToSkt;
	}
}