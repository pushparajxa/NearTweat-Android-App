package com.ist.neartweat.messages;

import java.util.ArrayList;

import com.ist.neartweat.platForm.Tweat;

public class RegistartionSuccessMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4011099987080472155L;
	ArrayList<Tweat> tweats;
	public ArrayList<Tweat> getTweats() {
		return tweats;
	}
	public void setTweats(ArrayList<Tweat> tweats) {
		this.tweats = tweats;
	}
}
