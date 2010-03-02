package com.googlecode.csse460_2010.server;

import java.util.HashMap;

/**
 * Room defines what a Room is and what it contains.
 * 
 * @author Christopher Rabotin
 */
public class Room {
	private final String name;
	private final Daemon meanny;
	private final HashMap<Directions, Room> exits = new HashMap<Directions, Room>(
			4);

	/**
	 * This enum class defines the valid directions.
	 * 
	 * @author Christopher Rabotin
	 * 
	 */
	private static enum Directions {
		/*
		 * Here are defined the valid directions.
		 */
		up, down, left, right;
		/**
		 * Converts a direction expressed as a String to its equivalent in the
		 * Direction enum class.
		 * 
		 * @param direction
		 *            the direction as a String
		 * @return the direction as a Direction
		 * @throws IllegalArgumentException
		 */
		public static Directions toDirection(String direction)
				throws IllegalArgumentException {
			Directions rtn = null;
			if (direction.equals("up")) {
				rtn = Directions.up;
			} else if (direction.equals("down")) {
				rtn = Directions.down;
			} else if (direction.equals("left")) {
				rtn = Directions.left;
			} else if (direction.equals("right")) {
				rtn = Directions.right;
			} else {
				Stirling.log.severe("Asked for direction " + direction
						+ " which doesn't exist!");
				throw new IllegalArgumentException("Unkown direction "
						+ direction);
			}
			return rtn;
		}

		/**
		 * Converts a direction expressed in the enum Direction to its
		 * equivalent in String.
		 * 
		 * @return a String representing the direction.
		 */
		@Override
		public String toString() {
			switch (this) {
			case up:
				return "up";
			case down:
				return "down";
			case left:
				return "left";
			case right:
				return "right";
			default:
				Stirling.log.severe("Asked for direction " + this
						+ " which doesn't exist!");
				return "null";
			}
		}
	};

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

	/**
	 * Adds an exit for this room. Since the exits are stored in a HashMap, if
	 * two exits are supposed to have the same direction (e.g. Room1 is UP from
	 * this room and Room2 is UP from this room as well) then only the second
	 * room is kept in memory (e.g. Room2).
	 * 
	 * @param e
	 *            Room instance of the exit
	 * @param direction
	 *            direction it is from the room.
	 */
	public void addExit(Room e, String direction) {
		try {
			exits.put(Directions.toDirection(direction), e);
			Stirling.log.config("Added exit " + e.getName() + " " + direction
					+ " from " + this.name);
		} catch (IllegalArgumentException ex) {
			/*
			 * This should only happen if the direction defined in the XML file
			 * is not a valid direction.
			 */
			Stirling.log.severe("Error! The direction " + direction
					+ " is invalid!");
			ex.printStackTrace();
			/*
			 * We stop the game immediately if there is an unknown direction in
			 * the XML file.
			 */
			System.exit(0);
		}
	}

	/**
	 * Returns the exit for the given direction. <b>Warning:</b> this function
	 * returns a null pointer if there is no exit for the given direction.
	 * 
	 * @param where
	 *            direction as a string
	 * @return the exit for the given direction
	 */
	public Room getExit(String where) {
		return exits.get(Directions.toDirection(where));
	}

	/**
	 * Returns the list of exits for this room. A typical exit is formatted as:
	 * exit:NAME. Exits in all directions are printed, the name is null if there
	 * is no exit.
	 * 
	 * @return the list of exits formatted (in raw data).
	 */
	public String getExitsFormatted() {
		String rtn = "";
		for (Directions d : Directions.values()) {
			try {
				String rn = exits.get(d).name;
				rtn += "exit:" + rn + "|";
			} catch (NullPointerException e) {
				rtn += "exit:null|";
			}
		}
		return rtn;
	}

	/**
	 * Getter for the name of the room.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the daemon in the room.
	 * 
	 * @return meanny
	 */
	public Daemon getMeanny() {
		return meanny;
	}
}
