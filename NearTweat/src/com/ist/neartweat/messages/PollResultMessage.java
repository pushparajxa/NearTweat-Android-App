package com.ist.neartweat.messages;

import com.ist.neartweat.platForm.Poll;

public class PollResultMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8972504862066832014L;
	private String pollId;
	private Poll poll;
	private String pollResultMessage;
	
public PollResultMessage(String pollId,Poll pl,String pollResult){
	setPollId(pollId);
	setPoll(pl);
	setPollResultMessage(pollResult);
}

public String getPollId() {
	return pollId;
}

public void setPollId(String pollId) {
	this.pollId = pollId;
}

public Poll getPoll() {
	return poll;
}

public void setPoll(Poll poll) {
	this.poll = poll;
}

public String getPollResultMessage() {
	return pollResultMessage;
}

public void setPollResultMessage(String pollResultMessage) {
	this.pollResultMessage = pollResultMessage;
}
}
