package com.ist.neartweat.platForm;

import java.io.Serializable;

public class Option implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1472831427355380148L;
	private int optionNumber;
	private String optionText;
	private int voteCount=0;
	
	public Option(int OptionNumber, String OptionText){
		this.optionNumber = OptionNumber;
		this.optionText = OptionText;
	}
	public int getOprionNumber() {
		return optionNumber;
	}
	public void setOprionNumber(int oprionNumber) {
		this.optionNumber = oprionNumber;
	}
	public String getOptionText() {
		return optionText;
	}
	public void setOptionText(String optionText) {
		this.optionText = optionText;
	}
	public int getVoteCount() {
		return voteCount;
	}
	public void setVoteCount(int voteCount) {
		this.voteCount = voteCount;
	}
	
	
}