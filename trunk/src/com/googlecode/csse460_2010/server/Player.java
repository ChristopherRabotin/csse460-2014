package com.googlecode.csse460_2010.server;

import java.util.ArrayList;

/**
 * Player contains all the necessary information about a player of the game.
 * 
 * @author Christopher Rabotin
 */
public class Player {
	public static enum States {
		IDLE, COMBAT, GAMEOVER
	};

	private States state;
	private Room room;
	private int points, health;
	private final int fullHealth;
	private final long connectionDate;
	private ArrayList<Attack> atks = new ArrayList<Attack>();
	private final String name;
	private final int id;
	private boolean isAlive, isBlessed = false;

	/**
	 * Player constructor.
	 * 
	 * @param name
	 *            Name of the player.
	 * @param fullHealth
	 *            Health the player starts with
	 */
	public Player(String name, int id, int fullHealth) {
		this.name = name;
		this.id = id;
		this.fullHealth = fullHealth;
		health = fullHealth;
		room = XMLParser.getDefaultRoom();
		points = 0;
		connectionDate = System.currentTimeMillis();
		state = States.IDLE;
		isAlive = true;
	}

	/**
	 * Adds an attack to the list of available attacks of the player.
	 * 
	 * @param e
	 *            an Attack to add.
	 */
	public void addAttack(Attack e) {
		atks.add(e);
	}

	/**
	 * Adds points to the player's points.
	 * 
	 * @param tba
	 *            number of points to be added to the player's current points.
	 */
	public void addPoints(int tba) {
		points += tba;
	}

	/**
	 * Subtracts health points to the player. If the player has no more points,
	 * the died() method is called.
	 * 
	 * @param ouch
	 *            number of health points to be subtracted from the player's
	 *            points
	 */
	public void lowerHealth(int ouch) {
		if (!isBlessed) {
			health -= ouch;
			if (health <= 0)
				isAlive = false;
		} else {
			health -= ouch / 10;
		}
	}

	public void reset() {
		health = fullHealth;
		room = XMLParser.getDefaultRoom();
		isAlive = true;
		state = States.IDLE;
	}

	// Generated getters and setters
	public void setRoom(Room r) {
		room = r;
		if (r.getMeanny().isAlive())
			state = States.COMBAT;
	}

	public Room getRoom() {
		return room;
	}

	public int getPoints() {
		return points;
	}

	public long getConnectionDate() {
		return connectionDate;
	}

	public int getHealth() {
		return health;
	}

	public ArrayList<Attack> getAtks() {
		return atks;
	}

	public int getFullHealth() {
		return fullHealth;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public void setState(States state) {
		this.state = state;
	}

	public States getState() {
		// this condition means that another player killed this daemon when both
		// were in the room
		Daemon meanny = room.getMeanny();
		if (meanny != null && !meanny.isAlive()) {
			state = States.IDLE;
		}
		return state;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void beatify() {
		isBlessed = true;
	}

	public boolean isBlessed() {
		return isBlessed;
	}
}
