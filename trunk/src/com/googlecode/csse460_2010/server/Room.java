package com.googlecode.csse460_2010.server;

import java.util.HashMap;

/**
 * Room defines what a Room is and what it contains.
 * 
 * @author Christopher Rabotin
 */
public class Room {
	public static enum Directions {
		up, down, left, right
	};

	private final String name;
	private final Daemon meanny;
	private final HashMap<Directions, Room> exits = new HashMap<Directions, Room>(
			4);

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

	public void addExit(Room e, String direction) {
		System.out.println("added room "+e.getName());
		try {
			exits.put(stringToDirection(direction), e);
		} catch (Exception ex) {
			System.err.println("Error in XML! The direction " + direction
					+ " is invalid!");
			ex.printStackTrace();
		}
	}

	public Room getExit(Directions where) {
		return exits.get(where);
	}

	public String getExitsFormatted() {
		String rtn = "";
		for (Directions d : exits.keySet()) {
			Room r = exits.get(d);
			rtn += directionToString(d) + ":" + r.name + ":"
					+ r.meanny.getName() + "\n";
		}
		return rtn;
	}

	public static Directions stringToDirection(String direction) {
		Directions rtn = null;
		if (direction.equals("up")) {
			rtn = Directions.up;
		} else if (direction.equals("down")) {
			rtn = Directions.down;
		} else if (direction.equals("left")) {
			rtn = Directions.left;
		} else if (direction.equals("right")) {
			rtn = Directions.right;
		}
		return rtn;
	}

	public static String directionToString(Directions d) {
		switch (d) {
		case up:
			return "up";
		case down:
			return "down";
		case left:
			return "left";
		case right:
			return "right";
		default:
			return "null";
		}
	}

	public String getName() {
		return name;
	}

	public Daemon getMeanny() {
		return meanny;
	}
}
