package com.googlecode.csse460_2010.server;

/**
 * Attack contains all the information about any attack retrieved from the XML
 * file.
 * 
 * @author Christopher Rabotin
 */
public class Attack {
	final String name;
	final int damage;
	final boolean daemonReserved;

	/**
	 * Attack constructor.
	 * 
	 * @param name
	 *            Name of the attack.
	 * @param damage
	 *            Damage the attack does to a player (can be negative i.e. gives
	 *            health to the player).
	 */
	public Attack(String name, int damage, boolean daemonOnly) {
		this.name = name;
		this.damage = damage;
		this.daemonReserved = daemonOnly;
	}

	public String getName() {
		return name;
	}

	public int getDamage() {
		return damage;
	}
}
