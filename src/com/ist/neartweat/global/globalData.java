package com.ist.neartweat.global;

import java.io.ObjectOutputStream;

import android.app.Application;

public class globalData extends Application{
	private static globalData singleton;
	private ObjectOutputStream ops;
	public globalData getInstance(){
		return singleton;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
	}
	
	public void setOps(ObjectOutputStream ops){
		this.ops = ops;
	}
	
	public ObjectOutputStream getOps(){
		return ops;
	}
	
	
}
