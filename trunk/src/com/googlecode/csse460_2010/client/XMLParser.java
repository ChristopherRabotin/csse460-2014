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

/**
 * This class takes care for parsing the XML configuration file. It also stores
 * all the data which can be retrieved using the getters defined at the end if
 * this class.
 * 
 * @author Christopher Rabotin
 * 
 */
public class XMLParser {
	private static int cnxPort, cnxTimeout, cnxPingTime;
	private static String cnxHost;
	private static Document xmlDoc;
	private static Element cnxRoot, cmdsRoot, gameMsgRoot;
	private static HashMap<String, Command> cmds = new HashMap<String, Command>();
	private static HashMap<String, String> serverMsgs = new HashMap<String, String>(),
			clientMsgs = new HashMap<String, String>();

	/**
	 * This function loads and parse the XML file xmlF. To parse it calls the
	 * methods parseCnx(), parseCmds() and parseGameMsg().
	 * 
	 * @param xmlF
	 *            the XML file to be parsed
	 * @throws JDOMException
	 *             if the XML is not properly formatted
	 * @throws IOException
	 *             if the XML file cannot be read
	 */
	public static void loadNParse(String xmlF) throws JDOMException,
			IOException {
		xmlDoc = (new SAXBuilder()).build(new File(xmlF));
		cnxRoot = xmlDoc.getRootElement().getChild("Cnx");
		cmdsRoot = xmlDoc.getRootElement().getChild("Cmds");
		gameMsgRoot = xmlDoc.getRootElement().getChild("GameMsgs");
		parseCnx();
		parseCmds();
		parseGameMsg();
	}

	/**
	 * This function parses the &lt;Cnx&gt;&lt;/Cnx&gt; element of the XML file.
	 * If there are multiple such element (which should NOT be the case), only
	 * the first one is taken into consideration. The read data is stored into
	 * cnxHost, cnxPort and cnxTimeout respectively for the host, the port and
	 * the timeout to establish the connection.
	 */
	private static void parseCnx() {
		cnxHost = cnxRoot.getAttributeValue("host");
		cnxPort = Integer.parseInt(cnxRoot.getAttributeValue("port"));
		cnxTimeout = Integer.parseInt(cnxRoot.getAttributeValue("timeout"));
		cnxPingTime = Integer.parseInt(cnxRoot.getAttributeValue("pingTime"));
	}

	/**
	 * Parses the &lt;Cmds&gt;&lt;/Cmds&gt; elements of the XML file. Each
	 * &lt;cmd&gt;&lt;/cmd&gt; element should have an &lt;args&gt;&lt;args&gt;
	 * element. The read data is stored in the cmds HashMap where the key is the
	 * client attribute of the element and the value is an instance of Command.
	 */
	@SuppressWarnings("unchecked")
	private static void parseCmds() {
		List cmdsXML = cmdsRoot.getChildren("cmd");
		Iterator cmdsXMLIt = cmdsXML.iterator();
		Element current, argsXML;
		Command cmd;
		while (cmdsXMLIt.hasNext()) {
			current = (Element) cmdsXMLIt.next();
			argsXML = current.getChild("args");
			cmd = new Command(current.getAttributeValue("client"), current
					.getAttributeValue("server"), current.getTextNormalize(),
					argsXML.getAttributeValue("clientValid"), argsXML
							.getAttributeValue("serverValid"), argsXML
							.getAttributeValue("special"));
			cmds.put(current.getAttributeValue("client"), cmd);
		}
	}

	/**
	 * Parses the &lt;GameMsgs&gt;&lt;/GameMsgs&gt; elements of the XML file.
	 * There are two types of such elements: the ClientMsg elements and the
	 * ServerMsg elements. The former are messages which will be printed to the
	 * client on the client's request (e.g. the welcome message is asked by the
	 * Client.java class on startup). The latter are translations of the server
	 * protocol. The parsed data is stored in two HashMaps: the client messages
	 * (ClientMsg) are stored in the clientMsgs Hashmap and the server messages
	 * are stored in the serverMsgs HashMap. In both cases the key corresponds
	 * to the <i>name</i> attribute (the <i>for</i> attribute for the case of
	 * serverMsg) and the value is the content String of the messages.
	 */
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
	 * Parses the given String (msg) to replace all the occurrences of variables
	 * with their actual value using reflection (java.lang.reflect). The
	 * variables are words starting with a $ sign.
	 * 
	 * @param msg
	 *            the message to be parsed by the function
	 * @param cls
	 *            the class (not an instance of the class) which contains the
	 *            fields of the text variables
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

	/**
	 * Getter of cnxHost.
	 * 
	 * @return cnxHost
	 */
	public static String getCnxHost() {
		return cnxHost;
	}

	/**
	 * Getter for cnxTimeout.
	 * 
	 * @return cnxTimeout
	 */
	public static int getCnxTimeout() {
		return cnxTimeout;
	}

	/**
	 * Getter of cnxPort.
	 * 
	 * @return cnxPort
	 */
	public static int getCnxPort() {
		return cnxPort;
	}
	
	/**
	 * Getter of cnxPingTime.
	 * 
	 * @return cnxPingTime
	 */
	public static int getCnxPingTime() {
		return cnxPingTime;
	}
	/**
	 * Getter for a particular client message.
	 * 
	 * @param key
	 *            the key which references the wanted value
	 * @return the value of the given key
	 * @throws IllegalArgumentException
	 *             if the key is not found an IllegalArgumentException is
	 *             thrown. This should only happen is the XML is not properly
	 *             designed (e.g. has been changed and does not fulfill the
	 *             requirements specified in the XML file).
	 */
	public static String getClientMsg(String key)
			throws IllegalArgumentException {
		try {
			return clientMsgs.get(key);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(key
					+ " is not a valid client message!");
		}
	}

	/**
	 * Getter for a particular server message.
	 * 
	 * @param key
	 *            the key which references the wanted value
	 * @return the value of the given key
	 * @throws IllegalArgumentException
	 *             if the key is not found an IllegalArgumentException is
	 *             thrown. This should only happen is the XML is not properly
	 *             designed (e.g. has been changed and does not fulfill the
	 *             requirements specified in the XML file).
	 */
	public static String getServerMsg(String key)
			throws IllegalArgumentException {
		try {
			return serverMsgs.get(key);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(key
					+ " is not a valid server message!");
		}
	}

	/**
	 * Getter for a particular command.
	 * 
	 * @param key
	 *            the key which references the wanted value
	 * @return the value of the given key
	 * @throws IllegalArgumentException
	 *             if the key is not found an IllegalArgumentException is
	 *             thrown. This happens if the player input an invalid command.
	 */
	public static Command getCmd(String key) throws IllegalArgumentException {
		try {
			return cmds.get(key);
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(key + " is not a valid command.");
		}
	}
}
