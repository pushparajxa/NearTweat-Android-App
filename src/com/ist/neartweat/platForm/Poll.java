package com.ist.neartweat.platForm;

import java.io.Serializable;
import java.util.ArrayList;

public class Poll implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1182839821262708851L;
	private String pollId;
	private String pollString;
	private String pollCreator;
	private ArrayList<Option> options= new ArrayList<Option>();
	public Poll(String PollCreator,String pollSrting){
		this.setPollCreator(PollCreator);
		setPollString(pollSrting);
	}
	
	public String getPollId() {
		return pollId;
	}
	public void setPollId(String pollId) {
		this.pollId = pollId;
	}
	public ArrayList<Option> getOptions() {
		return options;
	}
	public void setOptions(ArrayList<Option> options) {
		this.options.addAll(options);
	}
	public void addOption(Option opt){
		this.options.add(opt);
	}

	public String getPollString() {
		return pollString;
	}

	public void setPollString(String pollString) {
		this.pollString = pollString;
	}

	public String getPollCreator() {
		return pollCreator;
	}

	public void setPollCreator(String pollCreator) {
		this.pollCreator = pollCreator;
	}
	
	public Option getOption(int optionNumber){
		
		for(Option opt: options){
			if(opt.getOprionNumber()==optionNumber)
				return opt;
		}
		
		return null;
	}
	
	public String toString(){
		String s="Options and their resulst are";
		for(Option opt: options){
			s = s+opt.getOprionNumber()+" VoteCount="+opt.getVoteCount()+"\n";
		}
		return s;
	}
	
}


