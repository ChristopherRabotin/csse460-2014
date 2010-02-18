package com.googlecode.csse460_2010.server;

import java.util.ArrayList;

/**
 * Player contains all the necessary information about a player of the game.
 * 
 * @author Christopher Rabotin
 */
public class Player {
	private Room room;
	private int points, health;
	private final int fullHealth;
	private final long connectionDate;
	private final String name;
	private ArrayList<Attack> atks;

	/**
	 * Player constructor.
	 * 
	 * @param name
	 *            Name of the player.
	 * @param fullHealth
	 *            Health the player starts with
	 */
	public Player(String name, int fullHealth) {
		this.name = name;
		this.fullHealth = fullHealth;
		room = null;
		points = 0;
		connectionDate = System.currentTimeMillis();
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
		health -= ouch;
		if (health <= 0)
			died();
	}

	/**
	 * Determines what happens when a player dies.
	 */
	private void died() {
		// TODO What happens when the player dies?

	}

	// Generated getters and setters
	public void setRoom(Room r) {
		room = r;
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

	public String getName() {
		return name;
	}

	public int getHealth() {
		return health;
	}

	public ArrayList<Attack> getAtks() {
		return atks;
	}

}
