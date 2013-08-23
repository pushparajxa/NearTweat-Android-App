package com.ist.neartweat.messages;

public class PollAnswerMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1867393291172618244L;
	private String pollId;
	private int voteOption;
	private String voterId;
	public PollAnswerMessage(String voterId,String pollId, int voteOption){
		this.pollId = pollId;
		this.voteOption= voteOption;
		this.voterId = voterId;
	}
	public String getPollId() {
		return pollId;
	}
	public void setPollId(String pollId) {
		this.pollId = pollId;
	}
	public int getVoteOption() {
		return voteOption;
	}
	public void setVoteOption(int voteOption) {
		this.voteOption = voteOption;
	}
	public String getVoterId() {
		return voterId;
	}
	public void setVoterId(String voterId) {
		this.voterId = voterId;
	}

}
