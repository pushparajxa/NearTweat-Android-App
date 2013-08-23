package com.ist.neartweat;

import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.ist.neartweat.messages.NewTweatMessage;
import com.ist.neartweat.service.NetworkService;
import com.ist.neartweat.service.NetworkService.NetBinder;

public class TweatActivity extends Activity {
	NetworkService mService;
	boolean mBound = false;
	String ClientId;//This clientId will be the owner for the newly generated Tweat.

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweat);
		Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		Intent it = getIntent();

		this.ClientId = it.getStringExtra("com.ist.neartweat.ClientID");

	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweat, menu);
		return true;
	}
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			System.out.println("Connected to the service from Tweat activity");
			NetBinder binder = (NetBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	public void sendTweat(View v){
		/*
		 * Create a new TweatMessage class..send that Message to the server. Server will send the tweat to all other client.
		 * To send message call mService.sendMsg(msg)..The tweat owner id can be fetched by calling this.ClientId
		 * 
		 */
		EditText editText = (EditText) findViewById(R.id.TweatText);
		String tweatString = editText.getText().toString();
		NewTweatMessage ntm = new NewTweatMessage(this.ClientId,tweatString);
		try {
			mService.sendMsg(ntm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception while sending NewTweatMessage from the client="+this.ClientId);
			e.printStackTrace();
		}
		
	}

	/*@Override
	public void onBackPressed (){
		System.out.println("Unbinding from TweatActivity");
		//mService.unbindService(mConnection);
		//finish();
	}*/
}
