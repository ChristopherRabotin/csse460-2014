package com.googlecode.csse460_2010.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
	private static InputStream xmlFile = null;
	private static int serverPort = -1; // overrides the XMLParser one.
	public final static Logger log = Logger.getLogger(Stirling.class.getName());

	/**
	 * In the main function, Stirling starts by calling the XML parser. If no
	 * argument is given, than the default serverConfig.xml file is used as the
	 * configuration file.
	 * 
	 * @param args
	 *            you can specify the XML to use OR another port number. You
	 *            can't specify both.
	 */
	public static void main(String[] args) {
		LogHandler lh = new LogHandler(null);
		log.addHandler(lh);
		log.setLevel(Level.ALL);
		log.info("Starting game...");
		if (args.length > 0) {
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println(args[0]
						+ " is not a valid port number. Is a config file?");
				try {
					xmlFile = new FileInputStream(new File(args[0]));
					log.fine("Using configuration file " + xmlFile);
				} catch (FileNotFoundException e1) {
					System.err
							.println(args[0]
									+ " is not a port number and not a file ressource. Cannot proceed");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		if (xmlFile == null) {
			/*
			 * that means we're using the default file with another port
			 */
			xmlFile = Stirling.class.getResourceAsStream("serverConf.xml");
			log.fine("Using default configuration file (serverConf.xml)");
		}
		try {
			XMLParser.loadNParseXML(xmlFile);
			/*
			 * Check if the user specified a port number.
			 */
			if (serverPort == -1)
				serverPort = XMLParser.getServerPort();
			log.info("Loaded XML.");
		} catch (Throwable e) {
			log.severe("Error while loading XML:\n" + e);
			e.printStackTrace();
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
			/*
			 * Send messages to each client.
			 */
			if (whoasked.getId() == c.getId()) {
				/*
				 * NOTE: we use the thread's ID instead of the player's. We also
				 * do not yet kill the player who asked to kill the server.
				 */
				continue;
			}
			c.queueMsg("MCdying");
			c.killClient();
		}
		whoasked.killClient();
		MCServer.stopServer();
	}

	/**
	 * This messages enqueues the message msg as a multicast message to all
	 * users. It is not synchronized because it is only called by Stirling,
	 * which is static and has only one thread.
	 * 
	 * @param msg
	 *            multicast message
	 */
	public static void multicast(String msg) {
		players.trimToSize();
		for (Client c : players)
			c.queueMsg(msg);
	}

	/**
	 * Adds a player to list of players.
	 * 
	 * @param p
	 *            an instance of Client which represents the new player.
	 */
	synchronized public static void addPlayer(Client p) {
		multicast("MCjoin:" + p.getPlayer().getName());
		players.add(p);
		log.finest("Added player " + p.getPlayer().getName() + " (PlayerId="
				+ p.getPlayer().getId() + "; ThreadId=" + p.getId() + ")");
	}

	/**
	 * Removes a player to list of players.
	 * 
	 * @param p
	 *            an instance of Client which represents the player to be
	 *            removed.
	 */
	synchronized public static void rmPlayer(Client p) {
		players.remove(p);
		multicast("MCquit:" + p.getPlayer().getName());
		log.finest("Removed player " + p.getPlayer().getName() + " (PlayerId="
				+ p.getPlayer().getId() + "; ThreadId=" + p.getId() + ")");
	}

	/**
	 * Gets the number of players.
	 * 
	 * @return returns the number of current players.
	 */
	public static int getNoPlayers() {
		players.trimToSize();
		return players.size();
	}

	/**
	 * List of all the players, their health points and their current room.
	 * 
	 * @return the list of players formatted in raw data format.
	 */
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

	/**
	 * This function attacks the daemon which is in the room of player p with
	 * the given attack a. The daemon will reply to that attack if it is still
	 * alive. It is not synchronized because if it were the game would become a
	 * turn by turn game. However, the access to functions isAlive and attack
	 * from the daemon class are synchronized. In other words, is two players
	 * attack the same daemon at the same time, one will have to wait during the
	 * execution of his attack on the given daemon before this function returns.
	 * 
	 * @param p
	 *            the player launching the attack
	 * @param a
	 *            the attack used by player p
	 * @return the result of the attack.
	 */
	public static String attackDaemon(Player p, Attack a) {
		log.finest("Player " + p.getName() + " (PlayerId=" + p.getId()
				+ ") using attack " + a.getName());
		String rtn = "";
		Daemon d = p.getRoom().getMeanny();
		if (!d.isAlive()) {
			rtn = "nocombatBnobody"; // with two ds as dead daemon
		} else {
			Random r = new Random();
			float prob = r.nextFloat();
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
				rtn += "|hurty:" + ouch; // hurt you
				log.finest("Daemon " + d.getName() + " hurt player "
						+ p.getName() + " (PlayerId=" + p.getId() + " by "
						+ ouch + ") health points.");
				if (!p.isAlive()) {
					rtn += "|deady";
				}
			} else {
				log.finest("... and killed it!");
				p.addPoints(d.getVictoryPoints());
				p.setState(Player.States.IDLE);
				rtn += "|deadd";
				multicast("MCkilled:" + d.getName());
			}
		}
		checkAllDaemonsDead();
		return rtn;
	}

	/**
	 * Checks whether all the daemons are dead. If so, than the game is over. If
	 * the game is over, than a new game will start after the restart time
	 * specified in the server configuration XML file.
	 */
	public static void checkAllDaemonsDead() {
		for (Daemon grr : XMLParser.getDaemons().values()) {
			if (grr.isAlive())
				return; // no need to continue searching, at least one is alive.
		}
		log.info("All daemons are dead. The server will restart in "
				+ (XMLParser.getServerRestartTime()) / 1000 + " seconds.");
		multicast("MCwin:" + XMLParser.getServerRestartTime() / 1000);
		// now we restart the server after X seconds
		TimerTask resetTask = new TimerTask() {
			@Override
			public void run() {
				log.info("Restarting server now.");
				// start be setting all the players back to the original
				// room
				for (Client c : players) {
					c.getPlayer().reset();
					c.sendOnSkt("room:" + XMLParser.getDefaultRoom().getName());
				}
				for (String ds : XMLParser.getDaemons().keySet()) {
					XMLParser.getDaemons().get(ds).reset();
				}
				multicast("MCng");
			}
		};
		(new Timer()).schedule(resetTask, XMLParser.getServerRestartTime());

	}

	/**
	 * This function processes the client input, which is read from the socket.
	 * 
	 * @param player
	 *            the player who's input is being processed
	 * @param inputLn
	 *            the input to be processed
	 * @return a message for the client
	 */
	public static String processClientInput(Player player, String inputLn) {
		log.finest("Player " + player.getName() + "(id=" + player.getId()
				+ ") sent {" + inputLn + "}");
		String[] cmdargs = inputLn.split(" ");
		String outputLn = "", cmdarg = cmdargs[1]; // that's the argument
		if (inputLn.startsWith("go")) { /* GO */
			if (player.getState() != Player.States.IDLE) {
				outputLn = "nomove";
			} else {
				try {
					Room newRoom = player.getRoom().getExit(cmdarg);
					player.setRoom(newRoom);
					outputLn = "room:" + newRoom.getName();
					if (newRoom.getMeanny().isAlive()) {
						outputLn += "|fighting:"
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
				outputLn += "\nendraw";
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

			} else if (cmdarg.equals("myattacks")) { /* SHOW MYATTACKS */
				outputLn = "raw\n";
				outputLn += "Name\t\tDamage\n\n";
				ArrayList<Attack> atks = player.getAtks();
				for (Attack atk : atks) {
					outputLn += atk.getName() + "\t\t" + atk.getDamage() + "\n";
				}
				outputLn += "\nendraw";

			} else if (cmdarg.equals("points")) { /* SHOW POINTS */
				outputLn = "health:" + player.getHealth() + "/"
						+ player.getFullHealth() + "|points:"
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
					outputLn += "\n";
				}
				outputLn += "endraw";
			} else if (cmdarg.equals("exits")) { /* SHOW EXITS */
				outputLn = "exits:";
				outputLn += player.getRoom().getExitsFormatted();
			}
		} else if (inputLn.startsWith("attack")) { /* ATTACK */
			if (player.getState() != Player.States.COMBAT) {
				outputLn = "nocombatBnobody";
			} else if (!player.isAlive()) {
				outputLn = "nocombatBdeady";
			} else {
				boolean attacked = false;
				for (Attack e : player.getAtks()) {
					if (e.getName().toLowerCase().equals(cmdarg)) {
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
				Attack tbl = XMLParser.getAttacks().get(cmdarg.toLowerCase());
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
		} else if (inputLn.startsWith("iwin")) { /* IWIN */
			if (player.isBlessed()) {
				for (Daemon grr : XMLParser.getDaemons().values()) {
					if (grr.isAlive()) {
						grr.lowerHealth(grr.getFullHealth());
						multicast("MCkilled:" + grr.getName());
					}
				}
				/*
				 * We call checkAllDaemonsDead just to restart the server.
				 */
				checkAllDaemonsDead();
			} else {
				outputLn = "denied";
			}
		}
		return outputLn;
	}

	public static int getServerPort() {
		return serverPort;
	}
}
