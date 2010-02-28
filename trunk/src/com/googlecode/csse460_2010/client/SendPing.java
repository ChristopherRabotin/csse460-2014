package com.googlecode.csse460_2010.client;

import java.util.Timer;
import java.util.TimerTask;

public class SendPing extends TimerTask{
	private static Timer pingTimer = new Timer();
	/**
	 * The constructor is to called to initialize the timer task.
	 */
	public SendPing(){
		/*
		 * cnxPingTime is in seconds so me multiply it by 1000 to get milliseconds.
		 */
		pingTimer.scheduleAtFixedRate(this, 0, XMLParser.getCnxPingTime()*1000);
	}
	public static void init(){
		pingTimer.scheduleAtFixedRate(new SendPing(), 0, XMLParser.getCnxPingTime()*1000);
	}
	/**
	 * This is the method which takes care of sending the "ping" message to the
	 * server in order to retrieve multicast messages. We don't check whether
	 * the client is connected or not because the timer is only started once the
	 * client is connected.
	 */
	@Override
	public void run() {
		Client.getWriteToSkt().println("ping");
	}
	
}