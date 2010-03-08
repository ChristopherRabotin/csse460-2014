package com.googlecode.csse460_2010.client;

/**
 * This class stores all the available and valid commands defined in the XML
 * file.
 * 
 * @author Christopher Rabotin
 * 
 */
public class Command {
	private final String clientCmd, serverCmd, helpMsg, reflectVariable;
	private final String[] clientArgs, serverArgs;

	/**
	 * Constructor for Command. We store clientCmd even though it is not
	 * necessary because the object will be accessed via the hashMap in
	 * XMLParser.
	 * 
	 * @param serverCmd
	 *            the command on the server side
	 * @param helpMsg
	 *            the help message for this particular command
	 * @param clientArgs
	 *            the valid arguments on the client side, separated by a comma
	 * @param serverArgs
	 *            the valid arguments on the server side, separated by a comma
	 */
	public Command(String clientCmd, String serverCmd, String helpMsg,
			String clientArgs, String serverArgs, String special) {
		this.clientCmd = clientCmd;
		this.serverCmd = serverCmd;
		this.helpMsg = helpMsg;
		this.clientArgs = clientArgs.split(",");
		this.serverArgs = serverArgs.split(",");
		this.reflectVariable = special;
	}

	/**
	 * Check whether the passed argument is a valid client-side argument and
	 * returns the corresponding argument for the server.
	 * 
	 * @param arg
	 *            client side argument
	 * @return the command which should be understood by the server
	 * @throws IllegalArgumentException
	 *             if the parameter arg is not a valid client-side argument
	 */
	public String toServerCmd(String arg) throws IllegalArgumentException {
		String srvArg = null;
		if (serverArgs[0].equals("none")) {
			return serverCmd;
		} else if (clientArgs[0].equals("*")) {
			if (arg != null && !arg.equals("")) {
				return serverCmd + " " + arg;
			}
			throw new IllegalArgumentException(this.toString());
		} else {
			for (int i = 0; i < clientArgs.length; i++) {
				if (clientArgs[i].equals(arg))
					srvArg = serverArgs[i];
			}
			if (srvArg == null)
				throw new IllegalArgumentException(this.toString());
			return serverCmd + " " + srvArg;
		}
	}

	/**
	 * Returns whether this command needs an argument or not.
	 * 
	 * @return whether this command needs an argument or not.
	 */
	public boolean requestsArgument() {
		return !serverArgs[0].equals("none");
	}

	/**
	 * Getter of HelpMsg: the help message, i.e. the text contained in between
	 * the two delimiters of &lt;cmd&gt;.
	 * 
	 * @return helpMsg
	 */
	public String getHelpMsg() {
		return helpMsg;
	}

	/**
	 * Getter for the reflection variable, i.e. the name of the variable on the
	 * Client class.
	 * 
	 * @return reflectVariable
	 */
	public String getReflectVar() {
		return reflectVariable;
	}

	/**
	 * Overrides the toString function of Object. The output is cmd_name
	 * [cmd_arg1 | cmd_arg2 | ... ]
	 */
	@Override
	public String toString() {
		String rtn = clientCmd + " [";
		for (String tmp : clientArgs) {
			rtn += tmp + "|";
		}
		// TODO remove the trailing |
		return rtn + "]";
	}

	/**
	 * Getter for the serverCmd. It is used to know if the player wants to quit
	 * the game or not.
	 * 
	 * @return the serverCmd
	 */
	public String getServerCmd() {
		return serverCmd;
	}
}
