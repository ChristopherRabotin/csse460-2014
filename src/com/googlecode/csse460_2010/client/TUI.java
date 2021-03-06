package com.googlecode.csse460_2010.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TUI implements UIFactory {

	@Override
	public void mcMsg(String msg) {
		System.out.println(msg);
	}

	@Override
	public void stdMsg(String msg) {
		System.out.println(msg);
	}

	@Override
	public void getNSendCmdInput(String title) {
		String in = null, outputLn = null;
		do {
			in = getUserGlbInput(title,true);
			outputLn = Client.processClientInput(in);
		} while (outputLn == null);
		Client.sendOnSkt(outputLn);
		outputLn=null;
	}

	@Override
	public void errMsg(String msg) {
		System.err.println(msg);
	}

	@Override
	public String getUserGlbInput(String title, boolean mandatory) {
		stdMsg(title);
		String in = null;
		try {
			do{
			in = new BufferedReader(new InputStreamReader(System.in))
					.readLine();
			} while ((in == null || in.equals("")) && mandatory);
		} catch (IOException e) {
			System.err.println("Unable to read from input at this time!\n" + e);
		}
		return in;
	}

}
