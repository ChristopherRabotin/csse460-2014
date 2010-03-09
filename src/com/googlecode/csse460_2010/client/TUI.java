package com.googlecode.csse460_2010.client;

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
	public String getUserInput() {
		// TODO Auto-generated method stub
		return null;
	}

}
