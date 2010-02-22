package src.com.googlecode.csse460_2010.client;

public class Command {
	private final String clientCmd, serverCmd, helpMsg;
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
			String clientArgs, String serverArgs) {
		this.clientCmd = clientCmd;
		this.serverCmd = serverCmd;
		this.helpMsg = helpMsg;
		this.clientArgs = clientArgs.split(",");
		this.serverArgs = serverArgs.split(",");
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
		for (int i = 0; i < clientArgs.length; i++) {
			if (clientArgs[i].equals(arg))
				srvArg = serverArgs[i];
		}
		if (srvArg == null)
			throw new IllegalArgumentException(arg
					+ " is not a valid argument for " + this.toString());
		return serverCmd + " " + srvArg;
	}

	public String getHelpMsg() {
		return helpMsg;
	}

	@Override
	public String toString() {
		String rtn = clientCmd+" [";
		for (String tmp: clientArgs){
			rtn += tmp+"|";
		}
		//TODO remove the trailing |
		return rtn+"]";
	}
}
