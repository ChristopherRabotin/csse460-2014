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
	 * @param msg the message which is to be shown to the user.
	 */
	public void stdMsg(String msg);

	/**
	 * This method is called to print a multicast message.
	 * @param msg the message which is to be shown to the user.
	 */
	public void mcMsg(String msg);
	
	/**
	 * This method is called to get the user input.
	 * @return the user input
	 */
	public String getUserInput();
}
