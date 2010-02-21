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
	// these two fields are related to the server
	private static int serverPort, serverMaxConn;
	private static String serverName, serverWelcomeMsg;

	public static boolean loadNParseXML(String xmlF) {
		boolean error = false;
		try {
			xmlDoc = (new SAXBuilder()).build(new File(xmlF));
		} catch (JDOMException e) {
			e.printStackTrace();
			error = true;
		} catch (IOException e) {
			e.printStackTrace();
			error = true;
		}
		if (error)
			return false;
		serverRoot = xmlDoc.getRootElement().getChild("ServerConfig");
		protocolRoot = xmlDoc.getRootElement().getChild("Protocol");
		parseServerConf();
		gameRoot = xmlDoc.getRootElement().getChild("GameConfig");
		parseAttacks();
		parseDaemons();
		parseRooms();
		return true;
	}

	private static void parseServerConf() {
		serverPort = Integer.parseInt(serverRoot.getAttributeValue("port"));
		serverMaxConn = Integer.parseInt(serverRoot
				.getAttributeValue("maxPlayers"));
		serverName = serverRoot.getAttributeValue("name");
		serverWelcomeMsg = protocolRoot.getChildTextNormalize("WelcomeMsg");
		// now let's convert all the server properties of the welcome message to
		// their respective values
		serverWelcomeMsg = getWelcomeMsg("$", XMLParser.class);
		/*
		 * try { Class cls = XMLParser.class;
		 * 
		 * Field fieldlist[] = cls.getDeclaredFields(); for (int i = 0; i <
		 * fieldlist.length; i++) { Field fld = fieldlist[i];
		 * System.out.println("name = " + fld.getName());
		 * System.out.println("value = "+fld.get(cls));
		 * System.out.println("decl class = " + fld.getDeclaringClass());
		 * System.out.println("type = " + fld.getType()); int mod =
		 * fld.getModifiers(); System.out.println("modifiers = " +
		 * Modifier.toString(mod)); System.out.println("-----"); } } catch
		 * (Throwable e) { System.err.println(e); }
		 */
	}

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
	}

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

	@SuppressWarnings("unchecked")
	private static void parseRooms() {
		String name, meannyName;
		Room room;
		Daemon meanny;
		Element currentRoom, currentExit;
		List roomsList = gameRoot.getChild("Rooms").getChildren("room"), exitsList;
		Iterator roomsIt = roomsList.iterator(), exitsIt;
		while (roomsIt.hasNext()) {
			currentRoom = (Element) roomsIt.next();
			name = currentRoom.getAttributeValue("name");
			meannyName = currentRoom.getAttributeValue("daemon");
			if (!meannyName.equals("none"))
				meanny = daemons.get(meannyName);
			else
				meanny = null;
			room = new Room(name, meanny);

			exitsList = currentRoom.getChildren("exits");
			exitsIt = exitsList.iterator();
			while (exitsIt.hasNext()) {
				currentExit = (Element) exitsIt.next();
				room.addExit(rooms.get(currentExit.getAttributeValue("room")),
						currentExit.getAttributeValue("direction"));
			}

		}
		defaultRoom = rooms.get(gameRoot.getChild("Rooms").getAttributeValue(
				"default"));

	}

	public static HashMap<String, Daemon> getDaemons() {
		return daemons;
	}

	public static HashMap<String, Room> getRooms() {
		return rooms;
	}

	public static HashMap<String, Attack> getAttacks() {
		return attacks;
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static int getServerMaxConn() {
		return serverMaxConn;
	}

	public static String getServerName() {
		return serverName;
	}

	/**
	 * Parses the serverWelcomeMsg to replace all the occurrences of variables
	 * with their actual value
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
		return getWelcomeMsg(var, cls);
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
					System.out.println("type = " + fd.getType().toString());

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

	public static Room getDefaultRoom() {
		return defaultRoom;
	}

}
