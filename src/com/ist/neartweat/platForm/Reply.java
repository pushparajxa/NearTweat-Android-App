package com.ist.neartweat.platForm;

import java.io.Serializable;

public class Reply implements Serializable {
/**
	 * 
	 */
	private static final long serialVersionUID = 5257131004757347579L;
private String src;
private String dest;
private String content;
private long time;
public Reply(String src,String dest,String content,long time){
	this.setSrc(src);
	this.setDest(dest);
	this.setContent(content);
	this.setTime(time);
}
public String getSrc() {
	return src;
}
public void setSrc(String src) {
	this.src = src;
}
public String getDest() {
	return dest;
}
public void setDest(String dest) {
	this.dest = dest;
}
public String getContent() {
	return content;
}
public void setContent(String content) {
	this.content = content;
}
public long getTime() {
	return time;
}
public void setTime(long time) {
	this.time = time;
}

}
