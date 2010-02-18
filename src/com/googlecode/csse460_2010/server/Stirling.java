package com.googlecode.csse460_2010.server;

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
	private static ArrayList<Client> players;

	/**
	 * In the main function, Stirling starts by calling the XML parser. If no
	 * argument is given, than the default config.xml file is used as the
	 * configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length > 0){
			XMLParser.loadNParseXML(args[0]);
		}else{
			XMLParser.loadNParseXML("config.xml");
		}

	}
	
	public static void addPlayer(Client p){
		players.add(p);
	}
	
	public static void rmPlayer(Client p){
		players.remove(p);
	}
}
