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
			name = new BufferedReader(new InputStreamReader(System.in)).readLine();
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
			 * This method will block no more than timeoutMs. If the timeout
			 * occurs, SocketTimeoutException is thrown.
			 */
			skt.connect(sockaddr, timeout);
			readFromSkt = new BufferedReader(new InputStreamReader(skt
					.getInputStream()));
			writeToSkt = new PrintWriter(skt.getOutputStream(), true);

			writeToSkt.println("t");
			while ((inputLn = readFromSkt.readLine()) != null) {
				processServerMsg(inputLn);
				outputLn = processClientInput();
				if (outputLn != null) {
					writeToSkt.println(outputLn);
				}
			}

			readFromSkt.close();
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private static String processClientInput() {
		System.out.println(XMLParser.getClientMsg("input"));
		String toServer = null, in, cmd, arg, reflectVar;
		Field fd;
		try {
			in = new BufferedReader(new InputStreamReader(System.in)).readLine();
			cmd = in.split(" ")[0];
			arg = in.split(" ")[1];
			Command c = XMLParser.getCmd(cmd);
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
		} catch (IllegalArgumentException e) {
			System.out.println(XMLParser.getClientMsg("invalid"));
			System.err.println(e);
		} catch (IOException e){
			System.err.println("Unable to read from input at this time!\n"+e);
		}
		return toServer;
	}

	public static void processServerMsg(String str) {
		if (processRaw) {
			/*
			 * if we are processing a raw input then we simply output it, no
			 * questions asked
			 */
			System.out.println(str);
		} else if (str.startsWith("endraw")) {
			/*
			 * we don't print out anything as endraw is on it own line
			 */
			processRaw = false;
		} else if (!str.startsWith("raw")) {
			/*
			 * we don't print out anything as endraw is on it own line
			 */
			processRaw = true;
		} else {
			/*
			 * then we look up the meaning in the XMLParser class.
			 */
			String[] cmds = str.split(" "), args;
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
}