package com.googlecode.csse460_2010.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.swing.UIManager;

/**
 * This is the main class of the Client part of the project. As the
 * documentation specifies it, there are no input arguments necessary.
 * 
 * @author Christopher Rabotin
 * 
 */
public class Client {
	private static int port, timeout;
	private static String host, name, inputLn;
	private static InputStream xmlFile = Client.class
			.getResourceAsStream("clientConf.xml");
	private static InetAddress addr;
	private static SocketAddress sockaddr;
	private static Socket skt;
	private static boolean processRaw = false, quitting = false;
	private static BufferedReader readFromSkt;
	private static PrintWriter writeToSkt;
	private static Thread userInputThread;
	private static UIFactory ui;
	/*
	 * These variables will be used in the reflection which is why Java thinks
	 * they may not be used.
	 */
	@SuppressWarnings("unused")
	static private String latestDirection, latestAttack;

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
	 *            If "tui" is specified than the text user interface is used. If
	 *            there are two or more arguments, the two first must be host
	 *            and port number. The third one (optional) is "tui".
	 */
	public static void main(String[] args) {
		/*
		 * We start by reading the XML file to communicate with the server.
		 */
		try {
			XMLParser.loadNParse(xmlFile);
		} catch (Throwable e) {
			System.err.println("Error while loading the XML file!");
			e.printStackTrace();
			System.exit(0);
		}
		port = XMLParser.getCnxPort();
		timeout = XMLParser.getCnxTimeout();
		host = XMLParser.getCnxHost();
		/*
		 * Let's check if the user specified the host and port and find which
		 * user interface to use.
		 */
		if (args.length == 1 && args[0].contains("tui")) {
			ui = new TUI();
		} else if (args.length >= 2) {
			host = args[0];
			try {
				port = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				System.err.println(args[1] + " is not a valid port number.");
				System.exit(0);
			}
			if (args.length >= 3 && args[2].contains("tui")) {
				ui = new TUI();
			} else {
				/*
				 * We set the look in feel to the system's
				 */
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					System.err.println("System look n feel not supported.\n"
							+ "Switching to Java Metla LnF.");
					e.printStackTrace();
				}
				ui = new GUI();
			}
		} else {

			/*
			 * We set the look in feel to the system's
			 */
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				System.err.println("System look n feel not supported.\n"
						+ "Switching to Java Metla LnF.");
				e.printStackTrace();
			}
			ui = new GUI();
		}
		/*
		 * Then we print the welcome message to the user.
		 */
		ui.stdMsg(XMLParser.getClientMsg("welcome"));
		name = ui.getUserGlbInput(XMLParser.getClientMsg("askName"), true);
		ui.stdMsg(XMLParser.parseMsg(XMLParser.getClientMsg("nameThx"),
				XMLParser.class));
		try {
			addr = InetAddress.getByName(host);
			sockaddr = new InetSocketAddress(addr, port);

			skt = new Socket();

			/*
			 * This method will block no more than timeout (in milliseconds). If
			 * the timeout occurs, SocketTimeoutException is thrown.
			 */
			try {
				skt.connect(sockaddr, timeout);
			} catch (ConnectException e) {
				ui.errMsg(XMLParser.getClientMsg("cnxErr").replace("@",
						host + ":" + port));
				System.exit(0);
			}
			readFromSkt = new BufferedReader(new InputStreamReader(skt
					.getInputStream()));
			writeToSkt = new PrintWriter(skt.getOutputStream(), true);

			writeToSkt.println(name);
			/*
			 * Now we start the ping timer
			 */
			SendPing.init();

			while ((inputLn = readFromSkt.readLine()) != null && !quitting) {
				if (inputLn.length() > 0) {
					processServerMsg(inputLn);
					inputLn = "";
				}
				/*
				 * The server may send empty messages so we want to wait for
				 * actual data to arrive to ask for the user new input
				 */
				else
					continue;
				if (!processRaw
						&& (userInputThread == null || !userInputThread
								.isAlive())) {
					/*
					 * we create a new thread to deal with the input of the
					 * user. That way the MC messages send by the server are
					 * received in time. If we are already asking for user
					 * input, we don't ask again. There is only an esthetic
					 * problem since the last line is the one from the multicast
					 * message.
					 */

					userInputThread = new Thread("User Input Thread") {
						public void run() {
							/*
							 * We call the ui's get and n command input.
							 */
							ui
									.getNSendCmdInput(XMLParser
											.getClientMsg("input"));
						}
					};
					userInputThread.start();
				}
			}
			die();
		} catch (Throwable e) {
			ui.errMsg("Error! Shutting down three seconds after this message:"
					+ e.getMessage());
			e.printStackTrace();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
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
	 * @param in
	 *            the input to be tested as a valid (or not) command.
	 * @return the input of the user <i>translated</i> into the server protocol.
	 *         If the <i>translation</i> is not successful, null is returned.
	 *         <b>Warning:</b> the return value must be tested before sending
	 *         the information on the socket!
	 */
	public static String processClientInput(String in) {
		String toServer = null, cmd, arg, reflectVar;
		Field fd;
		try {
			in = in.toLowerCase(); /* case sensitive is a pain */
			cmd = in.split(" ")[0];
			Command c = XMLParser.getCmd(cmd);
			if (c == null) {
				throw new IllegalArgumentException(XMLParser.getClientMsg(
						"invalidCmd").replace("@", cmd));
			}
			if (c.requestsArgument()) {
				try {
					arg = in.split(" ")[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException(XMLParser.getClientMsg(
							"invalidArg").replace("@", c.toString()));
				}
				toServer = c.toServerCmd(arg);
				reflectVar = c.getReflectVar();
				if (reflectVar != null) {
					try {
						fd = Client.class.getDeclaredField(reflectVar);
						fd.setAccessible(true);
						fd.set(Client.class, arg);
					} catch (Throwable e) {
						ui
								.errMsg("No such field ["
										+ reflectVar
										+ "]. Make sure it is defined in client/Client.java.");
						ui.errMsg("" + e.getStackTrace());
						System.exit(0);
					}
				}
			} else
				toServer = c.toServerCmd("");
			if (c.getServerCmd().equals("bye")) { /* QUIT */
				quitting = true;
			} else if (c.getServerCmd().equals("help")) { /* HELP */
				toServer = null; /* to not send it to the server */
				try {
					arg = in.split(" ")[1];
				} catch (ArrayIndexOutOfBoundsException e) {
					throw new IllegalArgumentException(XMLParser.getClientMsg(
							"invalidArg").replace("@", c.toString()));
				}
				Command helpC = XMLParser.getCmd(arg);
				if (helpC == null) {
					throw new IllegalArgumentException(XMLParser.getClientMsg(
							"invalidCmd").replace("@", arg));
				}
				ui.stdMsg(helpC.getHelpMsg());
			}
		} catch (IllegalArgumentException e) {
			ui.errMsg(e.getMessage());
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
			ui.stdMsg(str);
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
				if (args[0].startsWith("MC")) {
					ui.mcMsg(toClient);
				} else {
					ui.stdMsg(toClient);
				}
			}
		}
	}

	/**
	 * Used to send information on the socket. It is used by the UIs and
	 * SendPing.<b>Warning:</b> this method does not test whether the
	 * information to be sent is correct or not!
	 * 
	 * @param msg
	 *            the message to be sent (without the trailing new line)
	 */
	public static void sendOnSkt(String msg) {
		writeToSkt.println(msg);
	}

	public static void die() {
		ui.stdMsg(XMLParser.getClientMsg("quit"));
		// writeToSkt.println("bye");
		SendPing.kill();
		try {
			readFromSkt.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		writeToSkt.close();
	}
}