package com.googlecode.csse460_2010.server;

import java.util.ArrayList;

/**
 * Player contains all the necessary information about a player of the game.
 * 
 * @author Christopher Rabotin
 */
public class Player {
	/**
	 * There are only two types of States for the player: IDLE and in COMBAT
	 * 
	 * @author Christopher Rabotin
	 * 
	 */
	public static enum States {
		IDLE, COMBAT
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
	 * Lower the player's points. This function is called only when the player
	 * <i>learns</i> a new attack.
	 * 
	 * @param tbl
	 */
	public void lowerPoints(int tbl) {
		points -= tbl;
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

	/**
	 * This method is called when a new game is about to start: it sends the
	 * player to the starting room, revives him/her and puts him in the IDLE
	 * state.
	 */
	public void reset() {
		health = fullHealth;
		room = XMLParser.getDefaultRoom();
		isAlive = true;
		state = States.IDLE;
	}

	// Generated getters and setters
	/**
	 * Changes the room of the player. If the new room has a daemon and it is
	 * alive, then the player's state is changed to COMBAT.
	 * 
	 * @param r
	 *            the room to which the player will be moved.
	 */
	public void setRoom(Room r) {
		room = r;
		if (r.getMeanny().isAlive())
			state = States.COMBAT;
	}

	/**
	 * Getter for the room of the player.
	 * 
	 * @return room
	 */
	public Room getRoom() {
		return room;
	}

	/**
	 * Getter for the player's points.
	 * 
	 * @return points
	 */
	public int getPoints() {
		return points;
	}

	/**
	 * Getter for the connection date of the player.
	 * 
	 * @return connectionDate
	 */
	public long getConnectionDate() {
		return connectionDate;
	}

	/**
	 * Getter for the player's health.
	 * 
	 * @return health
	 */
	public int getHealth() {
		return health;
	}

	/**
	 * Getter for the arraylist of the player's attacks.
	 * 
	 * @return atks
	 */
	public ArrayList<Attack> getAtks() {
		return atks;
	}

	/**
	 * Getter of the fullHealth variable.
	 * 
	 * @return fuulHealth
	 */
	public int getFullHealth() {
		return fullHealth;
	}

	/**
	 * Getter for the name of the player
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the id of the player.
	 * 
	 * @return id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Setter for the state of the player.
	 * 
	 * @param state
	 */
	public void setState(States state) {
		this.state = state;
	}

	/**
	 * Getter for the state of the player. If the daemon which is in the
	 * player's room is dead but the player is still in COMBAT state, then we
	 * update the state of this player to IDLE. This race condition occurs when
	 * two or more players are simultaneously fighting the same daemon: only the
	 * player who defeats it gets his/her state changed.
	 * 
	 * @return the state of the player.
	 */
	public States getState() {
		// this condition means that another player killed this daemon when both
		// were in the room
		Daemon meanny = room.getMeanny();
		if (meanny != null && !meanny.isAlive()) {
			state = States.IDLE;
		}
		return state;
	}

	/**
	 * Getter to check if the current player is still alive.
	 * 
	 * @return whether the player is alive or not.
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * Call this method to change the player into a God of the game. The attacks
	 * of a God are ten times as powerful and he/she only get ten times as less
	 * damage when getting an attack.
	 */
	public void beatify() {
		isBlessed = true;
	}

	/**
	 * Checks whether a player is a God or not.
	 * 
	 * @return whether the player is in godmode or not.
	 */
	public boolean isBlessed() {
		return isBlessed;
	}
	@Override
	public String toString(){
		return this.name+"("+this.id+")";
	}
}
