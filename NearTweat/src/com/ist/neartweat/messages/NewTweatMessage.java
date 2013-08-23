package com.ist.neartweat.messages;

public class NewTweatMessage extends Message{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2375245884334279068L;

	private String tweatCreator;
	private String tweatString;
	private byte[] photo;
	private boolean is_photo;
	public NewTweatMessage(String owner,String tweatString){
		this.setTweatCreator(owner);
		this.setTweatString(tweatString);
		setIs_photo(false);
	}
	public NewTweatMessage(String owner,byte[] photo){
		this.setTweatCreator(owner);
		this.setPhoto(photo);
		setIs_photo(true);
		tweatString = "This is a Photo Tweet";
	}
	public String getTweatCreator() {
		return tweatCreator;
	}
	public void setTweatCreator(String tweatCreator) {
		this.tweatCreator = tweatCreator;
	}
	public String getTweatString() {
		return tweatString;
	}
	public void setTweatString(String tweatString) {
		this.tweatString = tweatString;
	}
	public boolean isIs_photo() {
		return is_photo;
	}
	public void setIs_photo(boolean is_photo) {
		this.is_photo = is_photo;
	}
	public byte[] getPhoto() {
		return photo;
	}
	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}
}
