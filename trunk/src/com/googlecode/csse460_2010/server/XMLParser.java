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
	private static Element root;
	private static HashMap<String, Daemon> daemons;
	private static HashMap<String, Room> rooms;
	private static HashMap<String, Attack> attacks;

	public static boolean loadXML(String xmlF) {
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
		return rtn;
	}

	public static void parseAttacks(){
		List attacksList = root.getChild("Attacks").getChildren("attack");
		Iterator atkIt = attacksList.iterator();
		String name; int damage; boolean daemonOnly;
		while(atkIt.hasNext()){
			Element current = (Element) atkIt.next();
			name = current.getAttributeValue("name");
			damage = Integer.parseInt(current.getAttributeValue("damage"));
			daemonOnly = Boolean.parseBoolean(current.getAttributeValue("daemonOnly"));
			attacks.put(name, new Attack(name, damage, daemonOnly));
		}
	}
	
	public static void parseDaemons(){
		List daemonsList = root.getChild("Daemons").getChildren("daemon");
		Iterator daemonIt = daemonsList.iterator();
		String name, allAtks; int maxhealth, value;
		Daemon meanny;
		while(daemonIt.hasNext()){
			Element current = (Element) daemonIt.next();
			name = current.getAttributeValue("name");
			allAtks = current.getAttributeValue("attacks");
			maxhealth = Integer.parseInt(current.getAttributeValue("health"));
			value = Integer.parseInt(current.getAttributeValue("value"));
			meanny = new Daemon(name, maxhealth, value);
			// now we split the attacks and add each attack to the daemon
			String[] eachAtk = allAtks.split(",");
			for(String atk:eachAtk)
				meanny.addAttack(attacks.get(atk));
			daemons.put(name, meanny);
		}
	}
	
	public static void parseRooms() {
		String name;
		Room room;
		Daemon meanny;
		List roomsList = root.getChild("Rooms").getChildren("room"), exitsList;
		Iterator roomsIt = roomsList.iterator(), exitsIt;
		while (roomsIt.hasNext()) {
			Element current = (Element) roomsIt.next();
			name = current.getAttributeValue("name");
			
			exitsList = current.getChildren("exits");

		}

	}
}
