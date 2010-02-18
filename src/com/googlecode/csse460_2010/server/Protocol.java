package com.googlecode.csse460_2010.server;


/**
 * Protocol manages the understanding of communication between a Player and the game engine.
 * @author Christopher Rabotin
 *
 */
public class Protocol {

	private static enum PlayerStates {idle, fighting, eog};
	private static enum Cmds {go, show, help, info, exit, serverinfo};
	private static enum show {help, points, attacks, players, daemons};
	private static enum info {daemon, player};
	private static PlayerStates state=PlayerStates.idle;
	
}
