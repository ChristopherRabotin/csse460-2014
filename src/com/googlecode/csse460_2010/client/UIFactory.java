package com.googlecode.csse460_2010.client;

/**
 * This is a factory class which handles the user interface.
 * 
 * @author Christopher Rabotin
 * 
 */
public interface UIFactory {
	/**
	 * This method is called to print a standard message.
	 * 
	 * @param msg
	 *            the message which is to be shown to the user.
	 */
	public void stdMsg(String msg);

	/**
	 * This method is called to print a multicast message.
	 * 
	 * @param msg
	 *            the message which is to be shown to the user.
	 */
	public void mcMsg(String msg);

	/**
	 * This method is called to show an error message to the user.
	 * 
	 * @param msg
	 *            the message which is to be shown to the user
	 */
	public void errMsg(String msg);

	/**
	 * This method is called to get and write on socket the command input by the
	 * user. To write on the client socket, use Client.sendOnSkt(String).
	 * 
	 * @param title
	 *            the title of the input message
	 */
	public void getNSendCmdInput(String title);

	/**
	 * This method is called to get normal input.
	 * 
	 * @param title
	 *            the title of the input message
	 * @return the user's input
	 */
	public String getUserGlbInput(String title);
}
