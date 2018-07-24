package com.ist.neartweat.platForm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class ServerState implements Serializable{

	
	//Tweat Owner and list of tweats made by that user. Tweat Ids will start with <clientId>+1.
	private Hashtable<String,ArrayList<Tweat>> tweats = new  Hashtable<String,ArrayList<Tweat>>();
	
	private Hashtable<String,Integer> spamAccusationCount = new Hashtable<String,Integer>();
	private Hashtable<String,ArrayList<String>> accusedAndReporters = new Hashtable<String,ArrayList<String>>();
	private   ArrayList<String> spammers = new ArrayList<String>();
	private Hashtable<String,ArrayList<String>> pollIDandAnsweredClients = new Hashtable<String,ArrayList<String>>();
	private Hashtable<String,Poll> polls = new Hashtable<String,Poll>(); 
	private Hashtable<String,Integer> clientAndPollsCreatedCnt = new Hashtable<String,Integer>();
	private Hashtable<String,Poll> completedPolls = new Hashtable<String,Poll>();
	
	private static final long serialVersionUID = 2665583009579558751L;
	public ServerState(){
		
	}
	public Hashtable<String,ArrayList<Tweat>> getTweats() {
		return tweats;
	}
	public void setTweats(Hashtable<String,ArrayList<Tweat>> tweats) {
		this.tweats = tweats;
	}
	public Hashtable<String,Integer> getSpamAccusationCount() {
		return spamAccusationCount;
	}
	public void setSpamAccusationCount(Hashtable<String,Integer> spamAccusationCount) {
		this.spamAccusationCount = spamAccusationCount;
	}
	public Hashtable<String,ArrayList<String>> getAccusedAndReporters() {
		return accusedAndReporters;
	}
	public void setAccusedAndReporters(Hashtable<String,ArrayList<String>> accusedAndReporters) {
		this.accusedAndReporters = accusedAndReporters;
	}
	public Hashtable<String,ArrayList<String>> getPollIDandAnsweredClients() {
		return pollIDandAnsweredClients;
	}
	public void setPollIDandAnsweredClients(Hashtable<String,ArrayList<String>> pollIDandAnsweredClients) {
		this.pollIDandAnsweredClients = pollIDandAnsweredClients;
	}
	public  ArrayList<String> getSpammers() {
		return spammers;
	}
	public  void setSpammers(ArrayList<String> spammers) {
		this.spammers = spammers;
	}
	public Hashtable<String,Integer> getClientAndPollsCreatedCnt() {
		return clientAndPollsCreatedCnt;
	}
	public void setClientAndPollsCreatedCnt(Hashtable<String,Integer> clientAndPollsCreatedCnt) {
		this.clientAndPollsCreatedCnt = clientAndPollsCreatedCnt;
	}
	public Hashtable<String,Poll> getCompletedPolls() {
		return completedPolls;
	}
	public void setCompletedPolls(Hashtable<String,Poll> completedPolls) {
		this.completedPolls = completedPolls;
	}
	public Hashtable<String,Poll> getPolls() {
		return polls;
	}
	public void setPolls(Hashtable<String,Poll> polls) {
		this.polls = polls;
	}

}
