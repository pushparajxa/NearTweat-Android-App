package com.ist.neartweat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ist.neartweat.wifidirect.SimWifiP2pBroadcast;
import com.ist.neartweat.wifidirect.SimWifiP2pChangeInfo;
import com.ist.neartweat.wifidirect.SimWifiP2pDeviceList;
import com.ist.neartweat.wifidirect.SimWifiP2pInfo;

public class SimWifiP2pBroadcastReceiver extends BroadcastReceiver {

	private NetworkService mService;
	private MyServiceHandler mHandler;
	private Messenger mainActivityMsgr;
	public SimWifiP2pBroadcastReceiver(NetworkService activity,MyServiceHandler hdlr, Messenger mainActivityHander2) {
		super();
		Log.v("NearTweat","BroadCastReceiver Started");
		this.mService = activity;
		this.mHandler = hdlr;
		this.mainActivityMsgr = mainActivityHander2;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

			// This action is triggered when the WDSim service changes state:
			// - creating the service generates the WIFI_P2P_STATE_ENABLED event
			// - destroying the service generates the WIFI_P2P_STATE_DISABLED event

			int state = intent.getIntExtra(SimWifiP2pBroadcast.EXTRA_WIFI_STATE, -1);
			if (state == SimWifiP2pBroadcast.WIFI_P2P_STATE_ENABLED) {

				Log.v("NewarTweat:", "WiFi Direct enabled");

				/*Send this BroadCast to mainActivity */
				Message msg1 = Message.obtain();
				msg1.what  = NetworkService.P2P_STATE_ENABLED;
				//msg1.obj = ginfo;
				try {
					mainActivityMsgr.send(msg1);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					Log.v("NearTweat","Unable to send the message to the MainActivity");
					e.printStackTrace();
				}


			} else {

				Log.v("NewarTweat:",  "WiFi Direct disabled");


				/*Send this BroadCast to mainActivity */
				Message msg1 = Message.obtain();
				msg1.what  = NetworkService.P2P_STATE_DISABLED;

				try {
					mainActivityMsgr.send(msg1);
				} catch (RemoteException e) {

					Log.v("NearTweat","Unable to send the message to the MainActivity");
					e.printStackTrace();
				}
			}

		} else if (SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

			// Request available peers from the wifi p2p manager. This is an
			// asynchronous call and the calling activity is notified with a
			// callback on PeerListListener.onPeersAvailable()
			SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
			SimWifiP2pDeviceList deviceList = (SimWifiP2pDeviceList)intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);
			SimWifiP2pDeviceList devices = (SimWifiP2pDeviceList)intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_DEVICE_INFO);
			SimWifiP2pChangeInfo changeInfo= new SimWifiP2pChangeInfo(ginfo,deviceList,devices);

			Log.v("NearTweat:",  "Peer list changed,printing the info received");
			ginfo.print();

			Message msg = mHandler.obtainMessage(NetworkService.PEERS_CHANGED, changeInfo);

			mHandler.sendMessage(msg);


			/*Send this BroadCast to mainActivity */
			Message msg1 = Message.obtain();
			msg1.what  = NetworkService.PEERS_CHANGED;
			//msg1.obj = changeInfo;
			try {
				mainActivityMsgr.send(msg1);
			} catch (RemoteException e) {

				Log.v("NearTweat","Unable to send the message to the MainActivity");
				e.printStackTrace();
			}


		} else if (SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION.equals(action)) {

			SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
			SimWifiP2pDeviceList deviceList = (SimWifiP2pDeviceList)intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);
			SimWifiP2pChangeInfo changeInfo= new SimWifiP2pChangeInfo(ginfo,deviceList);
			ginfo.print();

			Log.v("NewarTweat:",  "Network membership changed");

			/*Sending this message to NetworkService to take Necessary acion */
			Message msg = mHandler.obtainMessage(NetworkService.NETWORK_MEMBERSHIP_CHANGED, changeInfo);
			mHandler.sendMessage(msg);
			/*Send this BroadCast to mainActivity */
			Message msg1 = Message.obtain();
			msg1.what  = NetworkService.NETWORK_MEMBERSHIP_CHANGED;
			msg1.obj = ginfo;
			try {
				mainActivityMsgr.send(msg1);
			} catch (RemoteException e) {

				Log.v("NearTweat","Unable to send the message to the MainActivity");
				e.printStackTrace();
			}


		} else if (SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION.equals(action)) {

			SimWifiP2pInfo ginfo = (SimWifiP2pInfo) intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_GROUP_INFO);
			SimWifiP2pDeviceList deviceList = (SimWifiP2pDeviceList)intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_DEVICE_LIST);
			SimWifiP2pDeviceList devices = (SimWifiP2pDeviceList)intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_DEVICE_INFO);
			SimWifiP2pChangeInfo changeInfo= new SimWifiP2pChangeInfo(ginfo,deviceList,devices);
			ginfo.print();

			Log.v("NewarTweat:",  "Group ownership changed");
			/*Sending this message to NetworkService to take Necessary acion */
			Message msg = mHandler.obtainMessage(NetworkService.GROUP_OWNERSHIP_CHANGED, changeInfo);

			mHandler.sendMessage(msg);

			Message msg1 = Message.obtain();
			msg1.what  = NetworkService.GROUP_OWNERSHIP_CHANGED;
			msg1.obj = ginfo;
			try {
				mainActivityMsgr.send(msg1);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.v("NearTweat","Unable to send the message to the MainActivity");
				e.printStackTrace();
			}
		}

		else if(SimWifiP2pBroadcast.WIFI_P2P_DEVICE_INFO_CHANGED_ACTION.equals(action)){
			Log.v("NearTweat","P2P Devices in its network Changed");
			SimWifiP2pDeviceList devices = (SimWifiP2pDeviceList)intent.getSerializableExtra(
					SimWifiP2pBroadcast.EXTRA_DEVICE_INFO);

			Message msg = mHandler.obtainMessage(NetworkService.DEVICE_INFO_CHANGED, devices);

			mHandler.sendMessage(msg);


			Message msg1 = Message.obtain();
			msg1.what  = NetworkService.DEVICE_INFO_CHANGED;
			msg1.obj = devices;
			try {
				mainActivityMsgr.send(msg1);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.v("NearTweat","Unable to send the message to the MainActivity");
				e.printStackTrace();
			}

		}
	}
}
