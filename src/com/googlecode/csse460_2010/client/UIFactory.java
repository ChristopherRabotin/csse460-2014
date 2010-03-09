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
	 */
	public void stdMsg();

	/**
	 * This method is called to print a multicast message.
	 */
	public void mcMsg();
}
