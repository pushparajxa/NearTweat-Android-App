package com.ist.neartweat.messages;

import com.ist.neartweat.platForm.Poll;

public class NewPollMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 251806546188136211L;
	private Poll poll;
	private String pollOwner;
	public NewPollMessage(String clientId,Poll pl){
		this.setPoll(pl);
		this.setPollOwner(clientId);
	}
	public Poll getPoll() {
		return poll;
	}
	public void setPoll(Poll poll) {
		this.poll = poll;
	}
	public String getPollOwner() {
		return pollOwner;
	}
	public void setPollOwner(String pollOwner) {
		this.pollOwner = pollOwner;
	}

}
