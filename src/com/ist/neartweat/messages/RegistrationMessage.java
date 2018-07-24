package com.ist.neartweat.messages;

public class RegistrationMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3249329180494256805L;
	private String clientId;
public RegistrationMessage(String clientId) {
		this.clientId = clientId;
	}
public String getClientId(){
	return clientId;
}
}
