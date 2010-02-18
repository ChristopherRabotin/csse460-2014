package com.googlecode.csse460_2010.server;

import java.util.ArrayList;

/**
 * Room defines what a Room is and what it contains.
 * 
 * @author Christopher Rabotin
 */
public class Room {
	private final String name;
	private final Daemon meanny;
	private final ArrayList<Room> exits = new ArrayList<Room>(4); 

	/**
	 * Room constructor.
	 * 
	 * @param name
	 *            Name of the room.
	 * @param meanny
	 *            Daemon in the room. There is only one daemon per room.
	 */
	public Room(String name, Daemon meanny) {
		this.name = name;
		this.meanny = meanny;
	}

	public void addExit(Room e){
		exits.add(e);
	}
	public String getName() {
		return name;
	}

	public Daemon getMeanny() {
		return meanny;
	}
}
