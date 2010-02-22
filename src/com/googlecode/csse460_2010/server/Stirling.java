package com.googlecode.csse460_2010.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Stirling is the main game engine. After starting the server, Stirling will
 * instantiate all the necessary elements for the game.
 * 
 * N.B.: The name originates from the compression engine of the same name.
 * 
 * @author Christopher Rabotin
 */
public class Stirling {
	private static ArrayList<Client> players = new ArrayList<Client>();
	private static String xmlFile = "config.xml";

	/**
	 * In the main function, Stirling starts by calling the XML parser. If no
	 * argument is given, than the default config.xml file is used as the
	 * configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Starting game...");
		if (args.length > 0) {
			xmlFile = args[0];
		}
		if (!XMLParser.loadNParseXML(xmlFile)) {
			System.out.println("Error while loading XML!");
		} else {
			System.out.println("Loaded XML.");
		}
		try {
			MCServer.startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server returned.");
	}

	@SuppressWarnings("unchecked")
	synchronized public static void endGame(Client whoasked) {
		ArrayList<Client> copyOfPlayer = (ArrayList<Client>) players.clone();
		for (Client c : copyOfPlayer) {
			try { // we wait for a couple seconds in order for all the multicast
				// messages to be received
				if (whoasked.getId() == c.getId()) { // NOTE: we use the
														// thread's ID
					// instead of the player's.
					continue; // we don't kill the person who asked yet
				}
				c.queueMsg("MC:Server shutting down. Good bye.");
				Thread.sleep(10000);
				c.killClient();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		whoasked.killClient();
		MCServer.stopServer();
	}

	synchronized public static void multicast(String msg) {
		players.trimToSize();
		for (Client c : players)
			c.queueMsg("MC:" + msg);
	}

	synchronized public static void addPlayer(Client p) {
		multicast("join " + p.getPlayer().getName());
		players.add(p);
	}

	synchronized public static void rmPlayer(Client p) {
		players.remove(p);
		multicast("quit " + p.getPlayer().getName());
	}

	public static int getNoPlayers() {
		players.trimToSize();
		return players.size();
	}

	public static String getPlayersFormatted() {
		players.trimToSize();
		String rtn = "Name\t\tHealth\t\tPoints\t\tRoom\n\n";
		for (Client c : players) {
			rtn += c.getPlayer().getName() + "\t\t" + c.getPlayer().getHealth()
					+ "/" + c.getPlayer().getFullHealth();
			rtn += "\t\t" + c.getPlayer().getPoints() + "\t\t"
					+ c.getPlayer().getRoom().getName();
			rtn += "\n";
		}
		return rtn;
	}

	public static String attackDaemon(Player p, Attack a) {
		String rtn = "";
		Daemon d = p.getRoom().getMeanny();
		if (!d.isAlive()) {
			rtn = "nocombat:deadd"; // with two ds as dead daemon
		} else {
			Random r = new Random();
			float prob = r.nextFloat();// *(1+p.getPoints());
			int ouch = Math.round(prob * a.getDamage());
			if (p.isBlessed())
				ouch *= 100;
			rtn = "hurtd:" + ouch; // hurt daemon
			d.lowerHealth(ouch);
			p.addPoints(ouch);
			// after being hit, the daemon responds (if still alive)
			if (d.isAlive()) {
				int whichOne = r.nextInt(d.getAttacks().size());
				prob = r.nextFloat();// *(1+p.getPoints());
				ouch = Math.round(prob
						* d.getAttacks().get(whichOne).getDamage());
				p.lowerHealth(ouch);
				rtn += " hurty:" + ouch; // hurt you
				if (!p.isAlive()) {
					rtn += " deady";
				}
			} else {
				p.addPoints(d.getVictoryPoints());
				p.setState(Player.States.IDLE);
			}
		}
		checkAllDaemonsDead();
		return rtn;
	}

	public static void checkAllDaemonsDead() {
		boolean allDead;
		for (String ds : XMLParser.getDaemons().keySet()) {
			if (XMLParser.getDaemons().get(ds).isAlive())
				allDead = false;
		}
		allDead = true;
		if (allDead) {
			multicast("victory. Reset in " + XMLParser.getServerRestartTime()
					+ " seconds.");
			// now we restart the server after X seconds
			TimerTask resetTask = new TimerTask() {
				@Override
				public void run() {
					// start be setting all the players back to the original
					// room
					for (Client c : players) {
						c.getPlayer().reset();
					}
					for (String ds : XMLParser.getDaemons().keySet()) {
						XMLParser.getDaemons().get(ds).reset();
					}
				}
			};
			(new Timer()).schedule(resetTask, XMLParser.getServerRestartTime());
		}
	}

	public static String processClientInput(Player player, String inputLn) {
		String[] cmdargs = inputLn.split(" ");
		String outputLn = "", tmparg = cmdargs[1]; // that's the subcommand
		if (inputLn.startsWith("go")) { /* GO */
			if (player.getState() != Player.States.IDLE) {
				outputLn = "nomove";
			} else {
				try {
					Room newRoom = player.getRoom().getExit(
							Room.stringToDirection(tmparg));
					player.setRoom(newRoom);
					outputLn = "room:" + newRoom.getName();
					if (newRoom.getMeanny().isAlive()) {
						outputLn += " state:fighting_"
								+ newRoom.getMeanny().getName();
					}
				} catch (NullPointerException e) {
					outputLn = "noexit:" + tmparg;
				}
			}
		} else if (inputLn.startsWith("show")) { /* SHOW */
			if (tmparg.equals("players")) { /* SHOW PLAYERS */
				outputLn = "raw\n";
				outputLn += Stirling.getPlayersFormatted();
			} else if (tmparg.equals("attacks")) { /* SHOW ATTACKS */
				outputLn = "raw\n";
				outputLn += "Name\t\tDamage\t\tDaemon reserved\n\n";
				HashMap<String, Attack> atks = XMLParser.getAttacks();
				Attack atk;
				for (String a : atks.keySet()) {
					atk = atks.get(a);
					outputLn += atk.getName() + "\t\t" + atk.getDamage()
							+ "\t\t" + atk.isDaemonReserved() + "\n";
				}

			} else if (tmparg.equals("points")) { /* SHOW POINTS */
				outputLn = "health:" + player.getHealth() + "/"
						+ player.getFullHealth() + " points:"
						+ player.getPoints();
			} else if (tmparg.equals("daemons")) { /* SHOW DAEMONS */
				outputLn = "raw\n";
				outputLn += "Name\t\tValue\t\tHealth\t\tAttacks\n";
				HashMap<String, Daemon> dae = XMLParser.getDaemons();
				Daemon tmp;
				for (String dn : dae.keySet()) {
					tmp = dae.get(dn);
					outputLn += tmp.getName() + "\t\t" + tmp.getVictoryPoints()
							+ "\t\t" + tmp.getHealth() + "/"
							+ tmp.getFullHealth() + "\t\t";
					for (Attack atk : tmp.getAttacks()) {
						outputLn += atk.getName() + " ";
					}
					outputLn += "\n";
				}
			} else if (tmparg.equals("exits")) { /* SHOW EXITS */
				outputLn = "raw\n";
				outputLn += player.getRoom().getExitsFormatted();
			}
		} else if (inputLn.startsWith("attack")) { /* ATTACK */
			if (player.getState() != Player.States.COMBAT) {
				outputLn = "nocombat:nobody";
			} else if (!player.isAlive()) {
				outputLn = "nocombat:deady";
			} else {
				boolean attacked = false;
				for (Attack e : player.getAtks()) {
					if (e.getName().equals(tmparg)) {
						outputLn = attackDaemon(player, e);
						attacked = true;
					}
				}
				if (!attacked) {
					outputLn = "nocombat:unkown_attack";
				}
			}
		}
		return outputLn;
	}
}
