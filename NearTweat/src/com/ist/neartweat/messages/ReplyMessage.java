package com.ist.neartweat.messages;

import com.ist.neartweat.platForm.Reply;

public class ReplyMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8725928242565684066L;
	private Reply r;
	private String tweatId;
	private boolean isPrivate;
	private long clientReceptionTime=0l;
	public ReplyMessage(){
		
	}
	
	public ReplyMessage(Reply r , String tweatId,boolean isPrivate){
		setR(r);
		setTweatId(tweatId);
		setPrivate(isPrivate);
	}

	public String getTweatId() {
		return tweatId;
	}

	public void setTweatId(String tweatId) {
		this.tweatId = tweatId;
	}

	public Reply getR() {
		return r;
	}

	public void setR(Reply r) {
		this.r = r;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public long getClientReceptionTime() {
		return clientReceptionTime;
	}

	public void setClientReceptionTime(long clientReceptionTime) {
		this.clientReceptionTime = clientReceptionTime;
	}

}
