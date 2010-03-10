package com.googlecode.csse460_2010.client;

import java.util.Timer;
import java.util.TimerTask;

public class SendPing extends TimerTask{
	private static Timer pingTimer = new Timer();
	/**
	 * This method initializes the timer task.
	 */
	public static void init(){
		pingTimer.scheduleAtFixedRate(new SendPing(), 0, XMLParser.getCnxPingTime());
	}
	/**
	 * This method stops the timer task.
	 */
	public static void kill(){
		pingTimer.cancel();
	}
	/**
	 * This is the method which takes care of sending the "ping" message to the
	 * server in order to retrieve multicast messages. We don't check whether
	 * the client is connected or not because the timer is only started once the
	 * client is connected.
	 */
	@Override
	public void run() {
		Client.sendOnSkt("ping");
	}
	
}