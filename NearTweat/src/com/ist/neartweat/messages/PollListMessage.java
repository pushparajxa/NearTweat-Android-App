package com.ist.neartweat.messages;

import java.util.ArrayList;

import com.ist.neartweat.platForm.Poll;

public class PollListMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3343194291708396646L;
	private ArrayList<Poll> polls =new ArrayList<Poll>();
	public PollListMessage(){
		
	}
	public void addPolls(ArrayList<Poll> pol){
		polls.addAll(pol);
	}
	public ArrayList<Poll> getPolls(){
		return polls;
	}
}
