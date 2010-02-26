package com.googlecode.csse460_2010.server;

/**
 * Attack contains all the information about any attack retrieved from the XML
 * file.
 * 
 * @author Christopher Rabotin
 */
public class Attack {
	private final String name;
	private final int damage;
	private final boolean daemonReserved;

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

	/**
	 * Getter for the name of the attack.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the damage this attack does.
	 * 
	 * @return damage.
	 */
	public int getDamage() {
		return damage;
	}

	/**
	 * Checks whether this attack is reserved for daemons only.
	 * 
	 * @return daemonReserved
	 */
	public boolean isDaemonReserved() {
		return daemonReserved;
	}

}
