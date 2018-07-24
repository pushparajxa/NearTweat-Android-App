package com.ist.neartweat.messages;

public class SpamMessage extends Message {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9185528588698720004L;
	private String reporterId;
	private String reportString;
	
	public SpamMessage(String reporterId, String reportString){
		setReporterId(reporterId);
		setReportString(reportString);
	}

	public String getReporterId() {
		return reporterId;
	}

	public void setReporterId(String reporterId) {
		this.reporterId = reporterId;
	}

	public String getReportString() {
		return reportString;
	}

	public void setReportString(String reportString) {
		this.reportString = reportString;
	}

}
