package com.googlecode.csse460_2010.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLParser {
	private static Document xmlDoc;
	private static Element serverRoot, gameRoot;
	private static HashMap<String, Daemon> daemons;
	private static HashMap<String, Room> rooms;
	private static HashMap<String, Attack> attacks;
	private static Room defaultRoom;
	private static int serverPort, serverMaxConn;

	public static boolean loadNParseXML(String xmlF) {
		boolean rtn = true;
		try {
			xmlDoc = (new SAXBuilder()).build(new File(xmlF));
		} catch (JDOMException e) {
			e.printStackTrace();
			rtn = false;
		} catch (IOException e) {
			e.printStackTrace();
			rtn = false;
		}
		serverRoot = xmlDoc.getRootElement().getChild("ServerConfig");
		parseServerConf();
		gameRoot = xmlDoc.getRootElement().getChild("GameConfig");
		parseAttacks();
		parseDaemons();
		parseRooms();
		return rtn;
	}

	private static void parseServerConf(){
		serverPort = Integer.parseInt(serverRoot.getAttributeValue("port"));
		serverMaxConn = Integer.parseInt(serverRoot.getAttributeValue("maxPlayers"));
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
		defaultRoom = rooms.get(gameRoot.getChild("Rooms").getAttributeValue("default"));

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
	
	public static Room getDefaultRoom(){
		return defaultRoom;
	}
	
}
