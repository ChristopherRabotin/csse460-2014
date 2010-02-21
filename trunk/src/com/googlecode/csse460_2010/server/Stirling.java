package com.googlecode.csse460_2010.server;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Stirling is the main game engine. After starting the server, Stirling will
 * instantiate all the necessary elements for the game.
 * 
 * N.B.: The name originates from the compression engine of the same name.
 * 
 * @author Christopher Rabotin
 */
public class Stirling {
	private static ArrayList<Client> players = new ArrayList<Client>();
	private static String xmlFile = "config.xml";

	/**
	 * In the main function, Stirling starts by calling the XML parser. If no
	 * argument is given, than the default config.xml file is used as the
	 * configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting game...");
		if (args.length > 0) {
			xmlFile = args[0];
		}
		if (!XMLParser.loadNParseXML(xmlFile)) {
			System.out.println("Error while loading XML!");
		} else {
			System.out.println("Loaded XML.");
		}
		try {
			MCServer.startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server returned.");
	}

	public static void addPlayer(Client p) {
		for(Client c : players)
			c.queueMsg("join "+p.getPlayer().getName());
		players.add(p);
	}

	public static void rmPlayer(Client p) {
		players.remove(p);
		for(Client c : players)
			c.queueMsg("quit "+p.getPlayer().getName());
	}

	public static int getNoPlayers() {
		return players.size();
	}

	public static String getPlayersFormatted() {
		String rtn = "Name\t\tHealth\t\tPoints\t\tRoom\n\n";
		for (Client c : players) {
			rtn += c.getPlayer().getName() + "\t\t" + c.getPlayer().getHealth()
					+ "/" + c.getPlayer().getFullHealth();
			rtn += "\t\t" + c.getPlayer().getPoints() + "\t\t"
					+ c.getPlayer().getRoom();
			rtn += "\n";
		}
		return rtn;
	}
}
