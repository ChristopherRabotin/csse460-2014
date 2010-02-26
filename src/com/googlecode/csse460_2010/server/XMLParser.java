package com.googlecode.csse460_2010.server;

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
 * XMLParser loads, read and parses the XML file. It also stores all the read
 * data to avoid using memory for duplicate entries.
 * 
 * @author Christopher Rabotin
 * 
 */
public class XMLParser {
	private static Document xmlDoc;
	private static Element serverRoot, gameRoot, protocolRoot;
	private static HashMap<String, Daemon> daemons = new HashMap<String, Daemon>();
	private static HashMap<String, Room> rooms = new HashMap<String, Room>();
	private static HashMap<String, Attack> attacks = new HashMap<String, Attack>();
	private static Room defaultRoom;
	private static Attack playersDefaultAttack;
	// these two fields are related to the server
	private static int serverPort, serverMaxConn, serverRestartTime;
	private static String serverName, serverWelcomeMsg;

	/**
	 * This function loads and parse the XML file xmlF. To parse it calls the
	 * methods parseServerConf(), parseAttacks(), parseDaemons() and
	 * parseRooms() (in that order).
	 * 
	 * @param xmlF
	 *            the XML file to be parsed
	 * @throws JDOMException
	 *             if the XML is not properly formatted
	 * @throws IOException
	 *             if the XML file cannot be read
	 */
	public static void loadNParseXML(String xmlF) throws JDOMException,
			IOException {
		xmlDoc = (new SAXBuilder()).build(new File(xmlF));
		serverRoot = xmlDoc.getRootElement().getChild("ServerConfig");
		protocolRoot = xmlDoc.getRootElement().getChild("Protocol");
		gameRoot = xmlDoc.getRootElement().getChild("GameConfig");
		parseServerConf();
		parseAttacks();
		parseDaemons();
		parseRooms();
		serverWelcomeMsg = getWelcomeMsg(serverWelcomeMsg, XMLParser.class);
	}

	/**
	 * This method parses the server configuration defined in the
	 * &lt;ServerConfig/&gt; element. It stores the attribute <i>port</i> in
	 * serverPort, the attribute <i>maxPlayers</i> in serverMaxConn, the
	 * attribute <i>name</i> in serverName and the attribute <i>restartTime</i>
	 * in serverRestartTime.
	 */
	private static void parseServerConf() {
		serverPort = Integer.parseInt(serverRoot.getAttributeValue("port"));
		serverMaxConn = Integer.parseInt(serverRoot
				.getAttributeValue("maxPlayers"));
		serverName = serverRoot.getAttributeValue("name");
		serverRestartTime = Integer.parseInt(serverRoot
				.getAttributeValue("restartTime"));
		serverRestartTime *= 1000;
		/*
		 * it is defined in seconds but TimerTask works in milliseconds
		 */
		serverWelcomeMsg = protocolRoot.getChildTextNormalize("WelcomeMsg");
		Stirling.log.config("Server has the following configuration:\nport = "
				+ serverPort + "\nname = " + serverName + "\nmaxConn = "
				+ serverMaxConn + "\nRestart time = " + serverRestartTime);
	}

	/**
	 * This method parses the Attacks, defined in the
	 * &lt;Attacks&gt;&lt;/Attacks&gt; element. Each
	 * &lt;attack&gt;&lt;/attack&gt; element defined the name, the damage and
	 * whether the attack is reserved for daemons. The parsed data is stored in
	 * the attacks HashMap where the key is the name of the daemon. This method
	 * also fetchs the default attack for each player, which is the attribute
	 * <i>playersDefaultAttack</i> of the second level
	 * &lt;Attack&gt;&lt;Attacks&gt; element.
	 */
	@SuppressWarnings("unchecked")
	private static void parseAttacks() {
		List attacksList = gameRoot.getChild("Attacks").getChildren("attack");
		Iterator atkIt = attacksList.iterator();
		String name;
		int damage;
		boolean daemonOnly;
		while (atkIt.hasNext()) {
			Element current = (Element) atkIt.next();
			name = current.getAttributeValue("name");
			damage = Integer.parseInt(current.getAttributeValue("damage"));
			daemonOnly = Boolean.parseBoolean(current
					.getAttributeValue("daemonOnly"));
			attacks.put(name, new Attack(name, damage, daemonOnly));
		}
		playersDefaultAttack = attacks.get(gameRoot.getChild("Attacks")
				.getAttributeValue("playersDefaultAttack"));
	}

	/**
	 * This method parses the Daemons, defined in the
	 * &lt;Daemons&gt;&lt;/Daemons&gt; element. Each
	 * &lt;daemon&gt;&lt;/daemon&gt; element defined the name, the health, the
	 * victory points and the attacks of each daemon. The parsed data is stored
	 * in the daemons HashMap where the key is the name of the daemon.
	 */
	@SuppressWarnings("unchecked")
	private static void parseDaemons() {
		List daemonsList = gameRoot.getChild("Daemons").getChildren("daemon");
		Iterator daemonIt = daemonsList.iterator();
		String name, allAtks;
		int maxhealth, value;
		Daemon meanny;
		while (daemonIt.hasNext()) {
			Element current = (Element) daemonIt.next();
			name = current.getAttributeValue("name");
			allAtks = current.getAttributeValue("attacks");
			maxhealth = Integer.parseInt(current.getAttributeValue("health"));
			value = Integer.parseInt(current.getAttributeValue("value"));
			meanny = new Daemon(name, maxhealth, value);
			// now we split the attacks and add each attack to the daemon
			String[] eachAtk = allAtks.split(",");
			for (String atk : eachAtk)
				meanny.addAttack(attacks.get(atk));
			daemons.put(name, meanny);
		}
	}

	/**
	 * This method parses the rooms in server's XML configuration file. The
	 * rooms are in the &lt;Rooms&gt;&lt;/Rooms&gt; second level element. Each
	 * room is defined in an &lt;room&gt;&lt;/room&gt; element. Each room can
	 * have up to four exits, one in each of the following directions:
	 * up,down,left,right. In this method, we start by parsing the rooms. Once
	 * that is done, we parse the exits. That enables the XML file to define
	 * rooms in what ever order and to reference them (via the &lt;exit/&gt;
	 * element) in whatever order as well.
	 */
	@SuppressWarnings("unchecked")
	private static void parseRooms() {
		String name, meannyName;
		Room room;
		Daemon meanny;
		Element currentRoom, currentExit;
		List roomsList = gameRoot.getChild("Rooms").getChildren("room"), exitsList;
		Iterator roomsIt = roomsList.iterator(), exitsIt;
		/*
		 * First we fill in the HashMap with all the rooms
		 */
		while (roomsIt.hasNext()) {
			currentRoom = (Element) roomsIt.next();
			name = currentRoom.getAttributeValue("name");
			meannyName = currentRoom.getAttributeValue("daemon");
			if (!meannyName.equals("none"))
				meanny = daemons.get(meannyName);
			else
				meanny = null;
			room = new Room(name, meanny);
			rooms.put(room.getName(), room);
		}
		/*
		 * Then for each defined room in XML, we fetch its exits and update the
		 * room.
		 */
		roomsIt = roomsList.iterator();
		while (roomsIt.hasNext()) {
			currentRoom = (Element) roomsIt.next();
			exitsList = currentRoom.getChildren("exit");
			exitsIt = exitsList.iterator();
			while (exitsIt.hasNext()) {
				currentExit = (Element) exitsIt.next();
				rooms.get(currentRoom.getAttributeValue("name")).addExit(
						rooms.get(currentExit.getAttributeValue("room")),
						currentExit.getAttributeValue("direction"));
			}
		}
		defaultRoom = rooms.get(gameRoot.getChild("Rooms").getAttributeValue(
				"default"));
		Stirling.log.config("Default room:"
				+ gameRoot.getChild("Rooms").getAttributeValue("default"));

	}

	/**
	 * Getter for all the daemons defined in the server configuration XML file.
	 * 
	 * @return daemons
	 */
	public static HashMap<String, Daemon> getDaemons() {
		return daemons;
	}

	/**
	 * Getter for all the rooms defined in the server configuration XML file.
	 * 
	 * @return rooms
	 */
	public static HashMap<String, Room> getRooms() {
		return rooms;
	}

	/**
	 * Getter for all the attacks defined in the server configuration XML file.
	 * 
	 * @return attacks
	 */
	public static HashMap<String, Attack> getAttacks() {
		return attacks;
	}

	/**
	 * Getter for serverPort.
	 * 
	 * @return serverPort
	 */
	public static int getServerPort() {
		return serverPort;
	}

	/**
	 * Getter for serverMaxConn, which is the maximum number of connections the
	 * server is allowed to accept.
	 * 
	 * @return serverMaxConn
	 */
	public static int getServerMaxConn() {
		return serverMaxConn;
	}

	/**
	 * Getter for the serverName.
	 * 
	 * @return serverName
	 */
	public static String getServerName() {
		return serverName;
	}

	/**
	 * Parses the serverWelcomeMsg to replace all the occurrences of variables
	 * with their actual value.
	 * 
	 * @param var
	 *            the first character of the variable ($ or @, etc.)
	 * @param obj
	 *            an instance which contains the fields of the text variables
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getWelcomeMsg(String var, Object obj) {
		Class cls = obj.getClass();
		String rtn = serverWelcomeMsg;
		String[] splt, splt1;
		Field fd;
		while (rtn.contains(var)) {
			splt = rtn.split("\\" + var); // we start by splitting
			// the string by the token (var)
			for (int i = 1; i < splt.length; i += 2) {
				// then for every even value (the one where the first word
				// corresponds to the field), we extract the first word
				splt1 = splt[i].split("\\W"); // we split by non-white character
				// to make sure we don't include
				// ! or .
				try {
					// finally we get the field in the given class, convert that
					// to string and replace the original string.
					fd = cls.getDeclaredField(splt1[0]);
					fd.setAccessible(true);

					rtn = rtn.replace(var + splt1[0], String.valueOf(fd
							.get(obj)));
				} catch (Throwable e) {
					System.out.println("[" + splt1[0] + "]");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		return rtn;
	}

	/**
	 * Parses the serverWelcomeMsg to replace all the occurrences of variables
	 * with their actual value. This function is to be used when there is no
	 * instance of the class. No need to worry though, Java takes care of which
	 * of the two functions to call. Java rocks!
	 * 
	 * @param var
	 *            the first character of the variable ($ or @, etc.)
	 * @param cls
	 *            the class which contains the fields of the text variables
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getWelcomeMsg(String var, Class cls) {
		String rtn = serverWelcomeMsg;
		String[] splt, splt1;
		Field fd;
		while (rtn.contains(var)) {
			splt = rtn.split("\\" + var); // we start by splitting
			// the string by the token (var)
			for (int i = 1; i < splt.length; i += 2) {
				// then for every even value (the one where the first word
				// corresponds to the field), we extract the first word
				splt1 = splt[i].split("\\W"); // we split by non-white character
				// to make sure we don't include
				// ! or .
				try {
					// finally we get the field in the given class, convert that
					// to string and replace the original string.
					fd = cls.getDeclaredField(splt1[0]);
					fd.setAccessible(true);
					rtn = rtn.replace(var + splt1[0], String.valueOf(fd
							.get(null)));
				} catch (Throwable e) {
					System.out.println("[" + splt1[0] + "]");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		return rtn;

	}

	/**
	 * Getter for defaultRoom.
	 * 
	 * @return defaultRoom
	 */
	public static Room getDefaultRoom() {
		return defaultRoom;
	}

	/**
	 * Getter for the playersDefaultAttack.
	 * 
	 * @return playersDefaultAttack
	 */
	public static Attack getPlayersDefaultAttack() {
		return playersDefaultAttack;
	}

	/**
	 * Getter for the serverRestartTime.
	 * 
	 * @return serverRestartTime
	 */
	public static int getServerRestartTime() {
		return serverRestartTime;
	}

}
