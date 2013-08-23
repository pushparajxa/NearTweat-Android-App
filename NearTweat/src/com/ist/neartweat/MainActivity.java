package com.ist.neartweat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ist.neartweat.service.NetworkService;
import com.ist.neartweat.service.NetworkService.NetBinder;

public class MainActivity extends Activity {

	NetworkService mService;
	boolean mBound = false;
	MyHandler mainActivityHdlr;
	Messenger msgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.v("NearTweat","From create method");
		mainActivityHdlr = new MyHandler(this);
		msgr = new Messenger(mainActivityHdlr);
		Log.v("NearTweat","Started MainActivityHandler and CreatedMainActivity");
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			Log.v("NearTweat","Connected to the Network service");
			NetBinder binder = (NetBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	public void sendState(View view){
		mService.sendStateInfo();
	}

	public void connectMe(View view){

		//Loop  until the service is created and bound with this Activity.

		while(mBound!=true){

		}

		Log.v("NearTweat","Connect method called");
		/*EditText server = (EditText)findViewById(R.id.ServerIpTextBox);
		final EditText clientId = (EditText)findViewById(R.id.ClientIdTextBox);
		Log.v("NearTweat","Serevr string is "+server.getText().toString()+" from connect method ");
		final String str[] =  server.getText().toString().split(":");
		Log.v("NearTweat","IP Address and port are "+str[0] + " "+str[1]);*/

		//boolean res = mService.connect(str[0],Integer.parseInt(str[1]),clientId.getText().toString());
		final EditText clientId = (EditText)findViewById(R.id.ClientIdTextBox);
		//boolean res = mService.connect("194.210.231.225",9876,clientId.getText().toString());
		//boolean res = mService.connect("194.210.230.114",9876,"raj1");
		//		if(res){
		//			Log.v("NearTweat","The connection successfully established");
		//		}
		//		else{
		//			Log.v("NearTweat","Unable to Connect");
		//		}
		mService.startWifi(msgr);
		Log.v("NearTweat","starting Second Activity");
		Intent intent = new Intent(this, SecondActivity.class);
		intent.putExtra("com.ist.nearTweat.ClientId", clientId.getText().toString());
		if(clientId.getText().toString().equalsIgnoreCase("A"))
		{
			//Do not start the second actvity.
		}
		else
		{
			startActivity(intent);
		}
		


	}

	static class MyHandler extends Handler{
		private MainActivity ma;
		public MyHandler(MainActivity ma){
			this.ma = ma;
		}
		public void  handleMessage (Message msg) {
			switch(msg.what){

			case NetworkService.GROUP_OWNERSHIP_CHANGED:
				Log.v("NearTweat","MainActivityHandler:Received GROUP_OWNERSHIP_CHANGED Broadcast");
				Toast.makeText(ma, "GROUP_OWNERSHIP_CHANGED",
						Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.NETWORK_MEMBERSHIP_CHANGED:
				Log.v("NearTweat","MainActivityHandler:Received NETWORK_MEMBERSHIP_CHANGED Broadcast");
				Toast.makeText(ma, "NETWORK_MEMBERSHIP_CHANGED",
						Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.PEERS_CHANGED:
				Log.v("NearTweat","MainActivityHandler:Received PEERS_CHANGED Broadcast");
				Toast.makeText(ma, "PEERS_CHANGED",
						Toast.LENGTH_SHORT).show();
				break;
			case  NetworkService.P2P_STATE_ENABLED :
				Log.v("NearTweat","MainActivityHandler:Received P2P_STATE_ENABLED Broadcast");
				Toast.makeText(ma, "WIFI_ENABLED",
						Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.P2P_STATE_DISABLED:
				Log.v("NearTweat","MainActivityHandler:Received P2P_STATE_DISABLED Broadcast");
				Toast.makeText(ma, "WIFI_DISABLED",
						Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.DEVICE_INFO_CHANGED:
				Log.v("NearTweat","MainActivityHandler:Received P2P_DEVICE_INFO_CHANGE Broadcast");
				Toast.makeText(ma, "DEVICE_INFO_CHANGED",
						Toast.LENGTH_SHORT).show();
				break;
				
			case NetworkService.CONNECTION_SUCCESS:
				Log.v("NearTweat","MainActivityHandler:Received CONNECTION_SUCCESS Broadcast");
				Toast.makeText(ma, "CONNECTION SUCCESS to the GO="+(String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.CONNECTION_CLOSE_TO_GO:
				Log.v("NearTweat","MainActivityHandler:Received CONNECTION_CLOSE_TO_GO Broadcast");
				Toast.makeText(ma, "CONNECTION CLOSE to the GO="+(String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.CONNECTION_CLOSE_TO_CLIENT:
				Log.v("NearTweat","MainActivityHandler:Received CONNECTION_CLOSE_TO_CLIENT Broadcast");
				Toast.makeText(ma, "CONNECTION_CLOSE_TO_CLIENT ="+(String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.STATE_INFO_RCVD:
				Log.v("NearTweat","MainActivityHandler:Received STATE_INFO_RCVD Broadcast");
				Toast.makeText(ma, "State Information Receive from the GO="+(String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.SENT_STATE_INFO:
				Log.v("NearTweat","MainActivityHandler:Received SENT_STATE_INFO Broadcast");
				Toast.makeText(ma, "State Information sent to the client ="+(String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case NetworkService.SERVER_START:
				Log.v("NearTweat","MainActivityHandler:Received SERVER_START Broadcast");
				Toast.makeText(ma, "Successfully Started the Server", Toast.LENGTH_SHORT).show();
				break;
				default:
					Log.v("NearTweat","Improper BroadCastReceived");
			}
		}
	}

}


