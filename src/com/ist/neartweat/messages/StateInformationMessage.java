package com.ist.neartweat.messages;

import com.ist.neartweat.platForm.ServerState;

public class StateInformationMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8461366347190659117L;
	private String Go;
	private ServerState srvrState;
	public StateInformationMessage (String go, ServerState state){
		this.Go = go;
		this.setSrvrState(state);
	}
	public String getGo() {
		return Go;
	}
	public void setGo(String go) {
		Go = go;
	}
	public ServerState getSrvrState() {
		return srvrState;
	}
	public void setSrvrState(ServerState srvrState) {
		this.srvrState = srvrState;
	}
}
