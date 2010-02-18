package com.googlecode.csse460_2010.server;

/**
 * Room defines what a Room is and what it contains.
 * 
 * @author Christopher Rabotin
 */
public class Room {
	final String name;
	final Daemon meanny;

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

	public String getName() {
		return name;
	}

	public Daemon getMeanny() {
		return meanny;
	}
}
