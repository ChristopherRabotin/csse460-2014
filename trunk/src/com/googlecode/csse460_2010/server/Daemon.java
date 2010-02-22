package com.googlecode.csse460_2010.server;

import java.util.ArrayList;
import java.util.Random;

/**
 * Daemon defines what a Daemon is.
 * 
 * @author Christopher Rabotin
 */
public class Daemon {
	private final int fullHealth, victoryPoints;
	private int health;
	private ArrayList<Attack> atks;
	private final String name;
	private boolean isAlive = true;

	/**
	 * Daemon constructor.
	 * 
	 * @param name
	 *            Name of the daemon
	 * @param maxhealth
	 *            Health the daemon starts with
	 * @param value
	 *            number of points all the attacking players share when
	 *            defeating this daemon.
	 */
	public Daemon(String name, int maxhealth, int value) {
		this.name = name;
		fullHealth = maxhealth;
		victoryPoints = value;
		health = fullHealth;
		atks = new ArrayList<Attack>();
	}

	/**
	 * Adds an attack to the list of available attacks of the daemon.
	 * 
	 * @param e
	 *            an Attack to add.
	 */
	public void addAttack(Attack e) {
		atks.add(e);
	}

	public ArrayList<Attack> getAttacks(){
		return atks;
	}
	/**
	 * This function lowers the health of the daemon. It is synchronized in case
	 * multiple players hit at the same time a same daemon.
	 * 
	 * @param ouch
	 */
	public synchronized void lowerHealth(int ouch) {
		if (!isAlive)
			return; // don't waste time lowering health is the daemon is dead.
		health -= ouch;
		if (health <= 0)
			isAlive = false;
	}
	/**
	 * Called when the daemon is supposed to attack.
	 * @return Returns the attack the players trying to defeat it will receive.
	 */
	public Attack attack(){
		Random random = new Random();
		return atks.get(random.nextInt(atks.size()));
	}
	
	public void reset(){
		health = fullHealth;
		isAlive = true;
	}
	
	public int getFullHealth() {
		return fullHealth;
	}

	public int getVictoryPoints() {
		return victoryPoints;
	}

	public int getHealth() {
		return health;
	}

	public String getName() {
		return name;
	}

	public boolean isAlive() {
		return isAlive;
	}
}
