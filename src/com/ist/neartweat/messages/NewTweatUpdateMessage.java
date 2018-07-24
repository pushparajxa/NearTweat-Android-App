package com.ist.neartweat.messages;

import com.ist.neartweat.platForm.Tweat;

public class NewTweatUpdateMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7547995822030993069L;
	private Tweat t;
	
	public NewTweatUpdateMessage(Tweat t){
		setTweat(t);
	}

	public Tweat getTweat() {
		return t;
	}

	public void setTweat(Tweat t) {
		this.t = t;
	}

}
