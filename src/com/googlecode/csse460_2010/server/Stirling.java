package com.googlecode.csse460_2010.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	private static String xmlFile = "serverConf.xml";
	public final static Logger log = Logger.getLogger(Stirling.class.getName());

	/**
	 * In the main function, Stirling starts by calling the XML parser. If no
	 * argument is given, than the default config.xml file is used as the
	 * configuration file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		LogHandler lh = new LogHandler(null);
		log.addHandler(lh);
		log.setLevel(Level.ALL);
		log.info("Starting game...");
		if (args.length > 0) {
			xmlFile = args[0];
		}
		log.fine("Using configuration file " + xmlFile);
		try {
			XMLParser.loadNParseXML(xmlFile);
			log.info("Loaded XML.");
		} catch (Throwable e) {
			log.severe("Error while loading XML:\n" + e);
		}
		try {
			MCServer.startServer();
			/*
			 * startServer is blocking function. It will NOT return until an
			 * order has been given.
			 */
			log.info("Server stopped.");
		} catch (IOException e) {
			log.severe("Error while starting server:\n" + e);
			e.printStackTrace();
		}
		System.exit(0);
	}

	@SuppressWarnings("unchecked")
	synchronized public static void endGame(Client whoasked) {
		log.info("Player " + whoasked.getPlayer().getName() + " (PlayerId="
				+ whoasked.getPlayer().getId() + ", ThreadId="
				+ whoasked.getId() + ") asked to kill the server.");
		ArrayList<Client> copyOfPlayer = (ArrayList<Client>) players.clone();
		for (Client c : copyOfPlayer) {
			// messages to be received
			if (whoasked.getId() == c.getId()) {
				/*
				 * NOTE: we use the thread's ID instead of the player's.
				 * We also do not yet kill the player who asked to kill the server.
				 */
				continue;
			}
			c.queueMsg("MC:Server shutting down. Good bye.");
			c.killClient();
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
		log.finest("Added player " + p.getPlayer().getName() + " (PlayerId="
				+ p.getPlayer().getId() + "; ThreadId=" + p.getId() + ")");
	}

	synchronized public static void rmPlayer(Client p) {
		players.remove(p);
		multicast("quit " + p.getPlayer().getName());
		log.finest("Removed player " + p.getPlayer().getName() + " (PlayerId="
				+ p.getPlayer().getId() + "; ThreadId=" + p.getId() + ")");
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
		log.finest("Player " + p.getName() + " (PlayerId=" + p.getId()
				+ ") using attack " + a.getName());
		String rtn = "";
		Daemon d = p.getRoom().getMeanny();
		if (!d.isAlive()) {
			rtn = "nocombat:deadd"; // with two ds as dead daemon
		} else {
			Random r = new Random();
			float prob = r.nextFloat();// *(1+p.getPoints());
			int ouch = Math.round(prob * a.getDamage());
			if (p.isBlessed())
				ouch *= 10;
			rtn = "hurtd:" + ouch; // hurt daemon
			log.finest("Player " + p.getName() + " (PlayerId=" + p.getId()
					+ ") hurt daemon " + d.getName() + " by " + ouch
					+ " health points.");
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
				log.finest("Daemon " + d.getName() + " hurt player "
						+ p.getName() + " (PlayerId=" + p.getId() + " by "
						+ ouch + ") health points.");
				if (!p.isAlive()) {
					rtn += " deady";
				}
			} else {
				log.finest("... and killed it!");
				p.addPoints(d.getVictoryPoints());
				p.setState(Player.States.IDLE);
			}
		}
		checkAllDaemonsDead();
		return rtn;
	}

	public static void checkAllDaemonsDead() {
		boolean allDead = true;
		for (String ds : XMLParser.getDaemons().keySet()) {
			if (XMLParser.getDaemons().get(ds).isAlive()) {
				allDead = false;
				break; // no need to continue searching, at least one is alive.
			}
		}
		if (allDead) {
			log.info("All daemons are dead. The server will restart in "
					+ (XMLParser.getServerRestartTime()) / 1000 + " seconds.");
			multicast("victory. Reset in " + XMLParser.getServerRestartTime()
					+ " seconds.");
			// now we restart the server after X seconds
			TimerTask resetTask = new TimerTask() {
				@Override
				public void run() {
					log.info("Restarting server now.");
					// start be setting all the players back to the original
					// room
					for (Client c : players) {
						c.getPlayer().reset();
					}
					for (String ds : XMLParser.getDaemons().keySet()) {
						XMLParser.getDaemons().get(ds).reset();
					}
					multicast("A new game has started.");
				}
			};
			(new Timer()).schedule(resetTask, XMLParser.getServerRestartTime());
		}
	}

	public static String processClientInput(Player player, String inputLn) {
		log.finest("Player " + player.getName() + "(id=" + player.getId()
				+ ") sent {" + inputLn + "}");
		String[] cmdargs = inputLn.split(" ");
		String outputLn = "", cmdarg = cmdargs[1]; // that's the subcommand
		if (inputLn.startsWith("go")) { /* GO */
			if (player.getState() != Player.States.IDLE) {
				outputLn = "nomove";
			} else {
				try {
					Room newRoom = player.getRoom().getExit(
							Room.stringToDirection(cmdarg));
					player.setRoom(newRoom);
					outputLn = "room:" + newRoom.getName();
					if (newRoom.getMeanny().isAlive()) {
						outputLn += " fighting:"
								+ newRoom.getMeanny().getName();
					}
				} catch (NullPointerException e) {
					outputLn = "noexit:" + cmdarg;
				}
			}
		} else if (inputLn.startsWith("show")) { /* SHOW */
			if (cmdarg.equals("players")) { /* SHOW PLAYERS */
				outputLn = "raw\n";
				outputLn += Stirling.getPlayersFormatted();
			} else if (cmdarg.equals("attacks")) { /* SHOW ATTACKS */
				outputLn = "raw\n";
				outputLn += "Name\t\tDamage\t\tDaemon reserved\n\n";
				HashMap<String, Attack> atks = XMLParser.getAttacks();
				Attack atk;
				for (String a : atks.keySet()) {
					atk = atks.get(a);
					outputLn += atk.getName() + "\t\t" + atk.getDamage()
							+ "\t\t" + atk.isDaemonReserved() + "\n";
				}
				outputLn += "\nendraw";

			} else if (cmdarg.equals("points")) { /* SHOW POINTS */
				outputLn = "health:" + player.getHealth() + "/"
						+ player.getFullHealth() + " points:"
						+ player.getPoints();
			} else if (cmdarg.equals("daemons")) { /* SHOW DAEMONS */
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
					outputLn += "\nendraw";
				}
			} else if (cmdarg.equals("exits")) { /* SHOW EXITS */
				outputLn = "raw\n";
				outputLn += player.getRoom().getExitsFormatted();
				outputLn += "\nendraw";
			}
		} else if (inputLn.startsWith("attack")) { /* ATTACK */
			if (player.getState() != Player.States.COMBAT) {
				outputLn = "nocombatBnobody";
			} else if (!player.isAlive()) {
				outputLn = "nocombatBdeady";
			} else {
				boolean attacked = false;
				for (Attack e : player.getAtks()) {
					if (e.getName().equals(cmdarg)) {
						outputLn = attackDaemon(player, e);
						attacked = true;
					}
				}
				if (!attacked) {
					outputLn = "nocombatBunkown";
				}
			}
		} else if (inputLn.startsWith("learn")) { /* LEARN */
			try {
				Attack tbl = XMLParser.getAttacks().get(cmdarg);
				if (tbl.isDaemonReserved()) {
					outputLn = "atkBdaemononly:" + cmdarg;
				} else if (player.getPoints() >= tbl.getDamage()) {
					player.addAttack(tbl);
					player.lowerPoints(tbl.getDamage());
					outputLn = "atk:" + cmdarg;
				} else {
					outputLn = "atkBnte_points:" + cmdarg;
				}
			} catch (NullPointerException e) {
				outputLn = "atkBunknown:" + cmdarg;
			}
		}
		return outputLn;
	}
}
