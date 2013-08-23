package com.ist.neartweat.platForm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class Tweat implements Cloneable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2695206164525917366L;
	private String tweatId;//It is a combination of owner and number. The number is give by the server.
	private String tweatString;
	private boolean isPhoto;
	private String owner;
	private byte[] Image;
	private ArrayList<Reply> publiReplies=  new ArrayList<Reply>();
	private Hashtable<String,ArrayList<Reply>> pvtReplies = new Hashtable<String,ArrayList<Reply>>();
	private long genTime;//The time when this tweas generated

	public Tweat(){

	}
	
	public Tweat(String owner, String id , String tweatstring){
		this.owner =  owner;
		this.tweatId = id;
		this.tweatString = tweatstring;
	}

	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public long getGenTime() {
		return genTime;
	}
	public void setGenTime(long genTime) {
		this.genTime = genTime;
	}
	public ArrayList<Reply> getPubliReplies() {
		return publiReplies;
	}

	public Hashtable<String,ArrayList<Reply>> getPvtReplies() {
		return pvtReplies;
	}

	public void addPubReply(Reply r){
		this.publiReplies.add(r);
	}
	public void addPvtReply(Reply r){
		if(pvtReplies.containsKey(r.getSrc())){
			pvtReplies.get(r.getSrc()).add(r);
		}
		else{
			pvtReplies.put(r.getSrc(), new ArrayList<Reply>());
			pvtReplies.get(r.getSrc()).add(r);
		}
	}
	public String getTweatString() {
		return tweatString;
	}
	public void setTweatString(String tweatString) {
		this.tweatString = tweatString;
	}
	public Object clone() throws CloneNotSupportedException{
		Tweat t = (Tweat)super.clone();
		t.publiReplies = new ArrayList<Reply>(this.publiReplies);
		return t;
	}

	public Tweat clone(String dest) throws CloneNotSupportedException{
		Tweat t = (Tweat) this.clone();
		if(t.getPvtReplies().containsKey(dest)){
			t.pvtReplies = new Hashtable<String,ArrayList<Reply>>();
			t.pvtReplies.put(dest, new ArrayList<Reply>(t.pvtReplies.get(dest)));
		}
		else{
			t.pvtReplies = new Hashtable<String,ArrayList<Reply>>();
		}
			
		return t;
	}
	public String getTweatId() {
		return tweatId;
	}
	public boolean isPhoto() {
		return isPhoto;
	}

	public void setIsPhoto(boolean isPhoto) {
		this.isPhoto = isPhoto;
	}

	public void setTweatId(String tweatId) {
		this.tweatId = tweatId;
	}
	
	public byte[] getImage(){
		return this.Image;
	}
	
	public void setImage(byte[] photo) {
		this.Image = photo;
		
	}
}
