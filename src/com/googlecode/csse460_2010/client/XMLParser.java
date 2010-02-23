package com.googlecode.csse460_2010.client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLParser {
	private static int cnxPort, cnxTimeout;
	private static String cnxHost;
	private static Document xmlDoc;
	private static Element cnxRoot, cmdsRoot, gameMsgRoot;
	private static HashMap<String, Command> cmds = new HashMap<String, Command>();
	private static HashMap<String, String> serverMsgs = new HashMap<String, String>(),
			clientMsgs = new HashMap<String, String>();

	public static void loadNParse(String xmlF) throws JDOMException,
			IOException {
		xmlDoc = (new SAXBuilder()).build(new File(xmlF));
		cnxRoot = xmlDoc.getRootElement().getChild("Cnx");
		cmdsRoot = xmlDoc.getRootElement().getChild("Cmds");
		gameMsgRoot = xmlDoc.getRootElement().getChild("GameMsgs");
		parseCnx();
		parseCmds();
		parseGameMsg();
		xmlDoc.getRootElement().getChildTextNormalize("WelcomeMsg");
	}

	private static void parseCnx() {
		cnxHost = cnxRoot.getAttributeValue("host");
		cnxPort = Integer.parseInt(cnxRoot.getAttributeValue("port"));
		cnxTimeout = Integer.parseInt(cnxRoot.getAttributeValue("timeout"));
	}

	@SuppressWarnings("unchecked")
	private static void parseCmds() {
		List cmdsXML = cmdsRoot.getChildren("cmds");
		Iterator cmdsXMLIt = cmdsXML.iterator();
		Element current, argsXML;
		Command cmd;
		while (cmdsXMLIt.hasNext()) {
			current = (Element) cmdsXMLIt.next();
			argsXML = current.getChild("args");
			cmd = new Command(current.getAttributeValue("client"), current
					.getAttributeValue("server"), current.getTextNormalize(),
					argsXML.getAttributeValue("serverValid"), argsXML
							.getAttributeValue("clientValid"), argsXML
							.getAttributeValue("special"));
			cmds.put(current.getAttributeValue("client"), cmd);
		}
	}

	@SuppressWarnings("unchecked")
	private static void parseGameMsg() {
		/*
		 * We start by getting all the messages which concern the game it-self.
		 */
		List msgsXML = gameMsgRoot.getChildren("ClientMsg");
		Iterator msgsXMLIt = msgsXML.iterator();
		Element current;
		while (msgsXMLIt.hasNext()) {
			current = (Element) msgsXMLIt.next();
			clientMsgs.put(current.getAttributeValue("name"), current
					.getTextNormalize());
		}
		/*
		 * Then we get the messages which are related to the information sent by
		 * the server.
		 */
		msgsXML = gameMsgRoot.getChildren("ServerMsg");
		msgsXMLIt = msgsXML.iterator();
		while (msgsXMLIt.hasNext()) {
			current = (Element) msgsXMLIt.next();
			serverMsgs.put(current.getAttributeValue("for"), current
					.getTextNormalize());
		}
	}

	/**
	 * Parses the serverWelcomeMsg to replace all the occurrences of variables
	 * with their actual value. The variables are words starting with a $ sign.
	 * 
	 * @param msg
	 *            the message to be parsed by the function
	 * @param cls
	 *            the class which contains the fields of the text variables
	 * @return the parsed string
	 */
	@SuppressWarnings("unchecked")
	public static String parseMsg(String msg, Class cls) {
		String[] splt, splt1;
		Field fd;
		while (msg.contains("$")) {
			/*
			 * We start by splitting the message by all the occurrences of
			 * variables.
			 */
			splt = msg.split("\\$");
			for (int i = 1; i < splt.length; i += 2) {
				/*
				 * then for every even value (the one where the first word
				 * corresponds to the field), we extract the first word (which
				 * is why we split by non-alpha numerical character).
				 */
				splt1 = splt[i].split("\\W");
				try {
					/*
					 * Finally we get the field in the given class, convert that
					 * to string and replace the original string.
					 */
					fd = cls.getDeclaredField(splt1[0]);
					fd.setAccessible(true);
					msg = msg.replace("$" + splt1[0], String.valueOf(fd
							.get(null)));
				} catch (Throwable e) {
					System.out.println("[" + splt1[0] + "]");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		return msg;
	}

	public static String getCnxHost() {
		return cnxHost;
	}

	public static int getCnxTimeout() {
		return cnxTimeout;
	}

	public static int getCnxPort() {
		return cnxPort;
	}

	public static String getClientMsg(String key)
			throws IllegalArgumentException {
		try {
			return clientMsgs.get(key);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(key
					+ " is not a valid client message!");
		}
	}

	public static String getServerMsg(String key)
			throws IllegalArgumentException {
		try {
			return serverMsgs.get(key);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(key
					+ " is not a valid server message!");
		}
	}

	public static Command getCmd(String key) throws IllegalArgumentException {
		try {
			return cmds.get(key);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(key
					+ " is not a valid command.");
		}
	}
}
