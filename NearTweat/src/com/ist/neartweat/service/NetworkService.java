package com.ist.neartweat.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ist.neartweat.messages.Message;
import com.ist.neartweat.messages.NewPollMessage;
import com.ist.neartweat.messages.NewTweatUpdateMessage;
import com.ist.neartweat.messages.PollListMessage;
import com.ist.neartweat.messages.PollResultMessage;
import com.ist.neartweat.messages.RegistartionSuccessMessage;
import com.ist.neartweat.messages.RegistrationMessage;
import com.ist.neartweat.messages.ReplyMessage;
import com.ist.neartweat.messages.StateInformationMessage;
import com.ist.neartweat.platForm.Reply;
import com.ist.neartweat.platForm.ServerState;
import com.ist.neartweat.server.NearTweatServer;
import com.ist.neartweat.wifidirect.SimWifiP2pBroadcast;
import com.ist.neartweat.wifidirect.SimWifiP2pChangeInfo;
import com.ist.neartweat.wifidirect.SimWifiP2pDeviceList;
import com.ist.neartweat.wifidirect.SimWifiP2pInfo;
import com.ist.neartweat.wifidirect.SimWifiP2pManager;
import com.ist.neartweat.wifidirect.SimWifiP2pManager.Channel;
import com.ist.neartweat.wifidirect.SimWifiP2pManager.GroupInfoListener;
import com.ist.neartweat.wifidirect.SimWifiP2pManager.MemberShipChangeListener;
import com.ist.neartweat.wifidirect.SimWifiP2pManager.PeerListListener;
import com.ist.neartweat.wifidirect.service.SimWifiP2pService;
import com.ist.neartweat.wifidirect.sockets.SimWifiP2pSocket;
import com.ist.neartweat.wifidirect.sockets.SimWifiP2pSocketManager;

public class NetworkService extends Service  {

	public static final int GROUP_OWNERSHIP_CHANGED =1;
	public static final int NETWORK_MEMBERSHIP_CHANGED=2;
	public static final int PEERS_CHANGED = 3;
	public static final int P2P_STATE_ENABLED = 4;
	public static final int P2P_STATE_DISABLED = 5;
	public static final int DEVICE_INFO_CHANGED = 6;
	public static final int CONNECTION_SUCCESS = 7;
	public static final int CONNECTION_TO_SERVER_SUCCESS = 8;
	public static final int CONNECTION_CLOSE_TO_GO = 9;
	public static final int CONNECTION_CLOSE_TO_CLIENT = 10;
	public static final int STATE_INFO_RCVD = 11;
	public static final int SENT_STATE_INFO = 12;
	public static final int SERVER_START = 13;
	boolean mBound = false;
	private Hashtable<String,ConnectThread> GoConns = new Hashtable<String,ConnectThread>();
	private ServerState srvrState=null;
 	/*End of vaiables for connection with SimWifiP2PService*/

	// Binder given to clients
	private final IBinder mBinder = new NetBinder();
	private ArrayList<Message> msgs = new ArrayList<Message>();
	private ArrayList<Message> pollMsgs = new ArrayList<Message>();
	private PollListMessage pollListMsg = new PollListMessage();//This is set when the service receives PollListMessage after RegistrationSuccessMessage.
	private Hashtable<String,ArrayList<ReplyMessage>> replyMsgs = new Hashtable<String,ArrayList<ReplyMessage>>();
	private String clientId;
	int msgReadCnt =0;
	MyServiceHandler msHdlr = new MyServiceHandler(this);
	Messenger  mainActivityMsgr;
	@Override
	public void onCreate(){
		super.onCreate();

	}

	private ServiceConnection mConnection = new ServiceConnection() {
		// callbacks for service binding, passed to bindService()

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {


			msHdlr.mService = new Messenger(service);
			msHdlr.mManager = new SimWifiP2pManager( msHdlr.mService);
			msHdlr.mChannel = msHdlr.mManager.initialize(NetworkService.this.getApplication(), NetworkService.this.getMainLooper(), null);
			initBroadCastReceiver(mainActivityMsgr);
			mBound = true;


		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			msHdlr.mService = null;
			msHdlr.mManager = null;
			msHdlr.mChannel = null;
			mBound = false;
		}
	};
	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class NetBinder extends Binder {
		public NetworkService getService() {
			// Return this instance of LocalService so clients can call public methods
			return NetworkService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	protected void initBroadCastReceiver(Messenger mainActivityHander2) {
		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
		filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);

		SimWifiP2pBroadcastReceiver receiver = new SimWifiP2pBroadcastReceiver(this,msHdlr,mainActivityHander2);
		registerReceiver(receiver, filter);


	}

	/** method for clients 
	 * @throws IOException */
	public synchronized void sendMsg(Message msg) throws IOException{
		if(msg instanceof ReplyMessage){

			ReplyMessage rm = (ReplyMessage)msg;
			msgs.add(msg);
			rm.setClientReceptionTime(System.currentTimeMillis());
			if(replyMsgs.containsKey(rm.getTweatId())){
				replyMsgs.get(rm.getTweatId()).add(rm);
			}
			else{
				ArrayList<ReplyMessage> r = new ArrayList<ReplyMessage>();
				r.add(rm);
				replyMsgs.put(rm.getTweatId(),r );
			}
		}

		for(String s: GoConns.keySet()){
			if(GoConns.get(s).ops!=null){
				GoConns.get(s).ops.writeObject(msg);
				Log.v("NearTweat","Sent Message to the Go="+s);
			}
		}

		/* 
		 * Add Code to handle the reuqest from activites for peersInRange and NetworksInRange(GroupsInRange) 
		 */
	}

	/** method for clients */
	public ArrayList<Message> giveMsgs(){
		if(msgReadCnt == msgs.size())
			return null;
		else{
			synchronized(msgs){//Synchronized because another thread in Connect method continuously populates the messages.
				ArrayList<Message>  al = new ArrayList<Message>(msgs.subList(msgReadCnt, msgs.size()));
				msgReadCnt = msgs.size();
				return al;
			}

		}

	}
	public String getClientId() {
		return clientId;
	}


	public void connect(String ip, int port,String clientId,String Go) {

		if(GoConns.containsKey(Go)){

		}
		else{
			this.clientId = clientId;
			ConnectThread ct =  new ConnectThread(ip,port,Go,this);
			GoConns.put(Go,ct );
			ct.start();

		}


	} 

	void addMsg(Message msg){

		if(msg instanceof PollListMessage){
			this.pollListMsg = (PollListMessage)msg;
			return;
		}

		else if(msg instanceof NewPollMessage){
			this.pollMsgs.add(msg);
			return;
		}

		else if(msg instanceof PollResultMessage){
			this.pollMsgs.add(msg);
			return;
		}
		else if(msg instanceof ReplyMessage){

			ReplyMessage rm = (ReplyMessage)msg;
			synchronized(msgs){
				msgs.add(msg);
			}
			rm.setClientReceptionTime(System.currentTimeMillis());

			if(replyMsgs.containsKey(rm.getTweatId())){
				replyMsgs.get(rm.getTweatId()).add(rm);
			}
			else{
				ArrayList<ReplyMessage> r = new ArrayList<ReplyMessage>();
				r.add(rm);
				replyMsgs.put(rm.getTweatId(),r );
			}
			return;
		}
		else if(msg instanceof NewTweatUpdateMessage){
			synchronized(msgs){
				msgs.add(msg);
			}
			return;
		}
		else if(msg instanceof StateInformationMessage){
			StateInformationMessage stateMsg = (StateInformationMessage)msg;
			this.setSrvrState(stateMsg.getSrvrState());
			String Go = stateMsg.getGo();
			this.GoConns.get(Go).recvStateInfo = true;
			return;
		}
		else if(msg instanceof RegistartionSuccessMessage){
			synchronized(msgs){
				Log.v("NearTweat","Receive Registration Success Message");
				msgs.add(msg);
			}
		}
	}

	public ArrayList<Message> givePollMsgs(){
		return this.pollMsgs;
	}

	public ArrayList<ReplyMessage> giveReplyMsgs(String tweatId, long timeStamp) {
		if(replyMsgs.containsKey(tweatId)){
			ArrayList<ReplyMessage> r = replyMsgs.get(tweatId);
			ArrayList<ReplyMessage> toSend = new ArrayList<ReplyMessage>();
			for(ReplyMessage re: r){
				if(re.getClientReceptionTime()>timeStamp){
					toSend.add(re);
				}
			}
			return toSend;
		}
		else{
			return null;
		}
	}

	public PollListMessage getPollListMsg() {
		return pollListMsg;
	}

	public Hashtable<String, ConnectThread> getGoConns() {
		return GoConns;
	}

	public void setPollListMsg(PollListMessage pollListMsg) {
		this.pollListMsg = pollListMsg;
	}

	public void startWifi(Messenger msgr) {

		Log.v("NearTweat","StartWifiMethod Called");
		mainActivityMsgr = msgr;
		Intent intent = new Intent(this, SimWifiP2pService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		//mBound=true;
		// initialize the WDSim API
		SimWifiP2pSocketManager.Init(getApplicationContext());

		//grpListner = new groupListener(this);
		//peerLsner = new peerListener(this);
	}

	public ServerState getSrvrState() {
		return srvrState;
	}

	public void setSrvrState(ServerState srvrState) {
		this.srvrState = srvrState;
	}

	public void sendStateInfo() {
		this.msHdlr.sendStateInfo();
		
		
	}


}
class ReplyWrapper{
	long receptionTime ;
	Reply reply;
	ReplyWrapper(long rtime, Reply r){
		this.receptionTime = rtime;
		this.reply = r;
	}

}

class ConnectThread extends Thread{

	String ip;
	int port;
	SimWifiP2pSocket sckt;
	boolean connected ;
	ObjectOutputStream ops;
	ObjectInputStream ois;
	String Go;
	NetworkService service;
	boolean readMsgs = true;
	boolean recvStateInfo=false;

	ConnectThread(String ip,int port,String Go,NetworkService service){

		this.ip = ip;
		this.port = port;
		this.Go = Go;
		this.service =  service;
	}
	public void run(){
		connected = false;
		while(!connected){

			try {

				sckt = new SimWifiP2pSocket(ip,port);

				InputStream iis = sckt.getInputStream();
				OutputStream oos = sckt.getOutputStream();
				ops = new ObjectOutputStream(oos);
				ops.flush();
				ois =  new ObjectInputStream(iis);
				connected = true;
				Log.v("NearTweat","Connection to the NearTweat Server is Successful");

			} catch (UnknownHostException e) {
				connected = false;
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				connected = false;
				e.printStackTrace();
			}
			if(!connected){

				if(sckt!=null){
					try {
						sckt.close();
						if(ops!=null){
							ops.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				long time = System.currentTimeMillis();
				while(System.currentTimeMillis() - time < 10000){
					//Wait for 10 seconds before attempting another connection.
				}

			}

		}

		if(connected){
			/*Send Connection Success message to the Application */
			android.os.Message msg1 = android.os.Message.obtain();
			msg1.what  = NetworkService.CONNECTION_SUCCESS;
			msg1.obj = this.Go;

			try {
				service.mainActivityMsgr.send(msg1);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.v("NearTweat","Sending Connection Success Message to the Client");
				e.printStackTrace();
			}
			/*End of Sending Connection Success message to the Application */

			try {

				RegistrationMessage reg = new RegistrationMessage(service.getClientId());
				ops.writeObject(reg);
				Log.v("NearTweat","Sent Registration Message to the GO="+this.Go);

				while(readMsgs){
					try {
						Message msg = (Message)ois.readObject();
						
						service.addMsg(msg);


					} catch (OptionalDataException e) {
						System.out.println("Except1");
						// TODO Auto-generated catch block
						e.printStackTrace();
						//readMsgs =false;
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						System.out.println("Except1");
						e.printStackTrace();
						//readMsgs =false;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println("Except1");
						e.printStackTrace();
						//readMsgs =false;
					}
				}

			}
			catch (UnknownHostException e) {
				System.out.println("Exception while connecting");
				e.printStackTrace();

			} catch (IOException e) {
				System.out.println("Exception while connecting");
				e.printStackTrace();

			}
		}

	}
	SimWifiP2pSocket getSocket(){
		return sckt;
	}
	ObjectOutputStream getOps(){
		return ops;
	}
	ObjectInputStream getIps(){
		return ois;
	}

	public void kill(boolean waitForStateInfoFromGO){
		if(waitForStateInfoFromGO == false){
			//do nothing
		}
		else{
			//Wait till receving the state info from the GO.
			while(this.recvStateInfo==false){
				Log.v("NearTweat","Waiting for the State Information Message");
			}
				
			
			/*Send Connection Success message to the Application */
			android.os.Message msg1 = android.os.Message.obtain();
			msg1.what  = NetworkService.STATE_INFO_RCVD;
			msg1.obj = this.Go;

			try {
				service.mainActivityMsgr.send(msg1);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				Log.v("NearTweat","State Information Receive from the GO="+this.Go);
				e.printStackTrace();
			}
		}

		this.readMsgs = false;
		try {
			this.ois.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.ops.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.sckt.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
		service.getGoConns().remove(this.Go);
		Log.v("NearTweat","Connection to the GO="+this.Go+" is removed.");

		/*Send Message to Application to display a Toast Saying that , it disconnected the connection to this Go*/
		android.os.Message msg1 = android.os.Message.obtain();
		msg1.what  = NetworkService.CONNECTION_CLOSE_TO_GO;
		msg1.obj = this.Go;
		try {
			service.mainActivityMsgr.send(msg1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}


class MyServiceHandler extends Handler{

	/*variables for Group related */

	boolean iamGo=false;
	boolean groupInfoRequestDone;
	boolean peerListRequestDone;

	SimWifiP2pDeviceList myDeviceList = new SimWifiP2pDeviceList();
	SimWifiP2pInfo myGroupInfo = new SimWifiP2pInfo() ;
	SimWifiP2pDeviceList myDevices = new SimWifiP2pDeviceList();
	NearTweatServer server;
	Hashtable<String,Socket> grpSockets = new Hashtable<String,Socket>();
	groupListener grpListner;
	peerListener peerLsner;
	/*End of Variables for Group related */

	/*Variables for connection with SimWifiP2PService */
	SimWifiP2pManager mManager = null;
	Channel mChannel = null;
	Messenger mService = null;
	NetworkService nws;

	public MyServiceHandler(NetworkService networkService){
		Log.v("NearTweat","NetworkService Handler Created");
		nws = networkService;
		grpListner = new groupListener(this);
		peerLsner = new peerListener(this);

	}
	public void sendStateInfo() {
		
					/*I am already GO . I have to cleanUP.
					 * I have to send the stateInformation to the GroupClients, then close connections */
					
					ArrayList<String> successList = getServer().sendStateInformation(myGroupInfo.getDeviceName());
					/* 
					 * Send Toast to the Application, Saying that stateInformation Delivered to the Client.
					 */
					for(String s:successList){
						
						/*Send Message to Application to display a Toast Saying that ,client disconnected from this group*/
						android.os.Message msg1 = android.os.Message.obtain();
						msg1.what  = NetworkService.SENT_STATE_INFO;
						msg1.obj = s;
						try {
							nws.mainActivityMsgr.send(msg1);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}					
					/*
					 * Ask Server to Clean the Clienthandles.
					 */
					//setIamGo(false);
					
				
			
		
		
	}
	@Override
	public void handleMessage(android.os.Message msg){
		switch(msg.what){
		case NetworkService.GROUP_OWNERSHIP_CHANGED:
			Log.v("NearTweat","MyServiceHandler: Handling GroupOwnerShipChanged");
			SimWifiP2pChangeInfo changeInfo = (SimWifiP2pChangeInfo)msg.obj;
			String deviceName = myGroupInfo.getDeviceName();
		
			
			myGroupInfo.mergeUpdate(changeInfo.getGinfo());
			myDeviceList.mergeUpdate(changeInfo.getDeviceList());
			myDevices .mergeUpdate(changeInfo.getDevices());

			SimWifiP2pInfo mInfo = changeInfo.getGinfo();
			//mInfo.getDeviceName();. This is the GroupName.GroupName is the GO deviceName.
			if(mInfo.askIsGO()){
				if(isIamGo()){
					/*I am already GO . nothing to do */
				}
				else{
					setIamGo(true);
					/* I have to start the server and handle all the incoming connections */
					if(nws.getSrvrState()!=null){
						setServer(new NearTweatServer(nws.getSrvrState()));
						nws.setSrvrState(null);
					}
					else{
						setServer(new NearTweatServer());
					}
					
					getServer().connect();
					
					/*Send Server Started Toast to the Application */
					/*Send Message to Application to display a Toast Saying that ,client disconnected from this group*/
					android.os.Message msg1 = android.os.Message.obtain();
					msg1.what  = NetworkService.SERVER_START;
					try {
						nws.mainActivityMsgr.send(msg1);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Log.v("NearTweat","Successfully Started the NearTweatServer");
					/*I connect to the Server I have hosted */
					if(myDevices.getByName(myGroupInfo.getDeviceName())!=null){
						Log.v("NearTweat","Connecting to the VirtualIp=" +myDevices.getByName(myGroupInfo.getDeviceName()).getVirtIp() + ".With DeviceName=" + myGroupInfo.getDeviceName());
						//connect(myDevices.getByName(myGroupInfo.getDeviceName()).getVirtIp(), 10001, myGroupInfo.getDeviceName());
					}
					Log.v("NewarTweat:","Successfully Started server from NetworkService as I am the GO");

				}
			}
			else{
				if(isIamGo()){
					/*I am already GO . I have to cleanUP.
					 * I have to send the stateInformation to the GroupClients, then close connections */
					
					ArrayList<String> successList = getServer().sendStateInformation(deviceName);
					/* 
					 * Send Toast to the Application, Saying that stateInformation Delivered to the Client.
					 */
					for(String s:successList){
						
						/*Send Message to Application to display a Toast Saying that ,client disconnected from this group*/
						android.os.Message msg1 = android.os.Message.obtain();
						msg1.what  = NetworkService.SENT_STATE_INFO;
						msg1.obj = s;
						try {
							nws.mainActivityMsgr.send(msg1);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}					
					/*
					 * Ask Server to Clean the Clienthandles.
					 */
					setIamGo(false);
					getServer().cleanUp();
				}
				else{
					/*I am not a GO. Nothing to do */

				}
			}

			break;

		case NetworkService.NETWORK_MEMBERSHIP_CHANGED:
			/*Some one came joined or left the group or a new group has been formed.
			 * Get the GO'd of the groups and connect to them.
			 */
			Log.v("NearTweat","MyServiceHandler: Handling NetWorkMemberrShipChanged.Requesting the peerList");
			SimWifiP2pChangeInfo NewInfo = (SimWifiP2pChangeInfo)msg.obj;

			if(isIamGo()){
				Set<String> oldgrp = myGroupInfo.getDevicesInNetwork();
				Set<String> newgrp = NewInfo.getGinfo().getDevicesInNetwork();
				if(oldgrp.size()!=0 && newgrp.size()!=0){//This will not handle the cases(newgrp.size()!=0
					if(newgrp.size()< oldgrp.size()){
						//Some groupClients left the group. Clear the handles and server Data for that clients.
						Set<String> dup = new TreeSet<String>(oldgrp);
						dup.removeAll(newgrp);
						for(String s: dup){
							Log.v("NearTweat","Cleaning up Since the Client "+s+",left the group "+myGroupInfo.getDeviceName());
							server.removeClient(s);

							/*Send Message to Application to display a Toast Saying that ,client disconnected from this group*/
							android.os.Message msg1 = android.os.Message.obtain();
							msg1.what  = NetworkService.CONNECTION_CLOSE_TO_CLIENT;
							msg1.obj = s;
							try {
								nws.mainActivityMsgr.send(msg1);
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}
				}
			}


			myDeviceList.mergeUpdate(NewInfo.getDeviceList());

			/*Compare for change in the Groups in which this node is memeber of */
			Set<String> disConnectedHomeGrps =new TreeSet<String>( myGroupInfo.getmHomeGroups());
			disConnectedHomeGrps.removeAll(NewInfo.getGinfo().getmHomeGroups());

			Set<String> newHomeGrps = new TreeSet<String>(NewInfo.getGinfo().getmHomeGroups());
			newHomeGrps.removeAll(myGroupInfo.getmHomeGroups());	

			if(disConnectedHomeGrps.size()==0 && newHomeGrps.size()==0){
				//doNothing . NoChange in HomeGroups.
			}
			else if(disConnectedHomeGrps.size()==0 && newHomeGrps.size()!=0 ){
				//Make Connections to the New Groups.
				for(String s: newHomeGrps){
					//Make connection to the GO node of that group, if you are not the GO of that Group.
					if(!s.equals(NewInfo.getGinfo().getDeviceName())){
						if(myDeviceList.getByName(s)!=null){

							nws.connect(myDeviceList.getByName(s).getVirtIp(), 10001, NewInfo.getGinfo().getDeviceName(),s);
							Log.v("NearTweat:","Making connection to the GO="+s+".From the Device="+NewInfo.getGinfo().getDeviceName());
						}
					}
				}
			}

			else if(disConnectedHomeGrps.size()!=0 && newHomeGrps.size()==0){
				//Stop Sending and Reading Messages from these disConnectedHomeGrps;
				for(String s: disConnectedHomeGrps){

					/*
					 * Case 1: You may have left the group. Or the GO may have left the group.
					 * 
					 * If you have left the group ,there will still be an entry in the mGroups of SimWifiP2pInfo.
					 * If the Go had left then there will no entry in the mGroups.Then you need to wait till you get message from the GO,
					 * which contains the state information.Then you receive Network_Membership_Changed Event where you connect to the new GO.
					 * The new GO will save the state and starts accepting the connections.					  
					 */
					if(!NewInfo.getGinfo().getmGroups().containsKey(s)){
					
						//Go left the group.

						if(nws.getGoConns().containsKey(s)){
							nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
							//
						}
						
						
					}
					else{
						
						//Means , you left the group or GO moved out of the range..So you can disconnect to the GO.
						if(NewInfo.getDeviceList().getByName(s)!=null){
							//Means Go is in the Range. So you left the group.
							if(nws.getGoConns().containsKey(s)){
								nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
							}
						}
						else{
							//Go is Out of the range. this case will be handled in the PEERS_CHANGED case, since 
							//When a GO moves out of your range , then you will get PEERS_CHANGED event..
							
						}
					}

				}

			}
			else {
				//disConnectedHomeGrps.size()!=0 && newHomeGrps.size()!=0
				//Make Connections to the new GO's.
				for(String s: newHomeGrps){
					//Make connection to the GO node of that group, if you are not the GO of that Group.
					if(!s.equals(NewInfo.getGinfo().getDeviceName())){
						if(myDeviceList.getByName(s)!=null){

							nws.connect(myDeviceList.getByName(s).getVirtIp(), 10001, NewInfo.getGinfo().getDeviceName(),s);
							Log.v("NearTweat:","Making connection to the GO="+s+".From the Device="+NewInfo.getGinfo().getDeviceName());
						}
					}
				}

				//Stop Sending and Reading Messages from these disConnectedHomeGrps;
				for(String s: disConnectedHomeGrps){

					/*
					 * Case 1: You may have left the group. Or the GO may have left the group.
					 * 
					 * If you have left the group ,there will still be an entry in the mGroups of SimWifiP2pInfo.
					 * If the Go had left then there will no entry in the mGroups.Then you need to wait till you get message from the GO,
					 * which contains the state information.Then you receive Network_Membership_Changed Event where you connect to the new GO.
					 * The new GO will save the state and starts accepting the connections.					  
					 */
					if(!NewInfo.getGinfo().getmGroups().containsKey(s)){
					
						//Go left the group.

						if(nws.getGoConns().containsKey(s)){
							nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
							//
						}
						
						
						
						
					}
					else{
						
						//Means , you left the group or GO moved out of the range..So you can disconnect to the GO.
						if(NewInfo.getDeviceList().getByName(s)!=null){
							//Means Go is in the Range. So you left the group.
							if(nws.getGoConns().containsKey(s)){
								nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
							}
						}
						else{
							//Go is Out of the range. this case will be handled in the PEERS_CHANGED case, since 
							//When a GO moves out of your range , then you will get PEERS_CHANGED event..
							
						}
					}

				}
			}

			myGroupInfo.mergeUpdate(NewInfo.getGinfo());

			Log.v("NearTweat","MyServiceHandler: Handling NetWorkMemberrShipChanged.Request of peerList Completed");
			break;

		case NetworkService.PEERS_CHANGED:
			Log.v("NearTweat","Received PEERS CHANGED in MyServiceHandler");
			SimWifiP2pChangeInfo Info = (SimWifiP2pChangeInfo)msg.obj;
			SimWifiP2pDeviceList newList = Info.getDeviceList();
			if(isIamGo()){
				if(myDeviceList.getDeviceList().size()!=0){
					if(newList.getDeviceList().size()==0){
						{
							
							//getServer().cleanUp();Do not do this here because, it is triggering the NetworkMemberShipEvent. Which will take care of removing the clients.
						}
					}
				}
			}
			else{
				if(myGroupInfo.getmHomeGroups().size()==0){
					//do nothing
				}
				else{
					for(String s: myGroupInfo.getmHomeGroups()){
						if(newList.getByName(s)==null){
							//The Go is No More in the Peer List.
							if(this.nws.getGoConns().containsKey(s)){
								if(nws.getGoConns().containsKey(s)){
									nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
									//
								}
							}
							myGroupInfo.getmGroups().remove(s);
							Log.v("NearTweat","FROM PEER_CHANGED Case::");
						}
						else{
							//do nothing
						}
					}
				}
			}
			this.myDeviceList.mergeUpdate(Info.getDeviceList());
			break;
			
		case NetworkService.DEVICE_INFO_CHANGED:
			Log.v("NearTweat","MyServiceHandle handling the change in the device information");
			SimWifiP2pDeviceList devices  = (SimWifiP2pDeviceList)msg.obj;
			myDevices.mergeUpdate(devices);
			break;
		default:
			super.handleMessage(msg);

		}
	}
	
	public void process(android.os.Message msg){
		/*Some one came joined or left the group or a new group has been formed.
		 * Get the GO'd of the groups and connect to them.
		 */
		Log.v("NearTweat","MyServiceHandler: Handling NetWorkMemberrShipChanged.Requesting the peerList");
		SimWifiP2pChangeInfo NewInfo = (SimWifiP2pChangeInfo)msg.obj;

		if(isIamGo()){
			Set<String> oldgrp = myGroupInfo.getDevicesInNetwork();
			Set<String> newgrp = NewInfo.getGinfo().getDevicesInNetwork();
			if(oldgrp.size()!=0 && newgrp.size()!=0){//This will not handle the cases(newgrp.size()!=0
				if(newgrp.size()< oldgrp.size()){
					//Some groupClients left the group. Clear the handles and server Data for that clients.
					Set<String> dup = new TreeSet<String>(oldgrp);
					dup.removeAll(newgrp);
					for(String s: dup){
						Log.v("NearTweat","Cleaning up Since the Client "+s+",left the group "+myGroupInfo.getDeviceName());
						server.removeClient(s);

						/*Send Message to Application to display a Toast Saying that ,client disconnected from this group*/
						android.os.Message msg1 = android.os.Message.obtain();
						msg1.what  = NetworkService.CONNECTION_CLOSE_TO_CLIENT;
						msg1.obj = s;
						try {
							nws.mainActivityMsgr.send(msg1);
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		}


		myDeviceList.mergeUpdate(NewInfo.getDeviceList());

		/*Compare for change in the Groups in which this node is memeber of */
		Set<String> disConnectedHomeGrps =new TreeSet<String>( myGroupInfo.getmHomeGroups());
		disConnectedHomeGrps.removeAll(NewInfo.getGinfo().getmHomeGroups());

		Set<String> newHomeGrps = new TreeSet<String>(NewInfo.getGinfo().getmHomeGroups());
		newHomeGrps.removeAll(myGroupInfo.getmHomeGroups());	

		if(disConnectedHomeGrps.size()==0 && newHomeGrps.size()==0){
			//doNothing . NoChange in HomeGroups.
		}
		else if(disConnectedHomeGrps.size()==0 && newHomeGrps.size()!=0 ){
			//Make Connections to the New Groups.
			for(String s: newHomeGrps){
				//Make connection to the GO node of that group, if you are not the GO of that Group.
				if(!s.equals(NewInfo.getGinfo().getDeviceName())){
					if(myDeviceList.getByName(s)!=null){

						nws.connect(myDeviceList.getByName(s).getVirtIp(), 10001, NewInfo.getGinfo().getDeviceName(),s);
						Log.v("NearTweat:","Making connection to the GO="+s+".From the Device="+NewInfo.getGinfo().getDeviceName());
					}
				}
			}
		}

		else if(disConnectedHomeGrps.size()!=0 && newHomeGrps.size()==0){
			//Stop Sending and Reading Messages from these disConnectedHomeGrps;
			for(String s: disConnectedHomeGrps){

				/*
				 * Case 1: You may have left the group. Or the GO may have left the group.
				 * 
				 * If you have left the group ,there will still be an entry in the mGroups of SimWifiP2pInfo.
				 * If the Go had left then there will no entry in the mGroups.Then you need to wait till you get message from the GO,
				 * which contains the state information.Then you receive Network_Membership_Changed Event where you connect to the new GO.
				 * The new GO will save the state and starts accepting the connections.					  
				 */
				if(NewInfo.getGinfo().getmGroups().containsKey(s)){
					//Means , you left the group.So you can disconnect to the GO.
					
					if(nws.getGoConns().containsKey(s)){
						nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
					}
				}
				else{
					//Go left the group.

					if(nws.getGoConns().containsKey(s)){
						nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.State Information Already been received because we ae manually sending it.
						//
					}
					
				}

			}

		}
		else {
			//disConnectedHomeGrps.size()!=0 && newHomeGrps.size()!=0
			//Make Connections to the new GO's.
			for(String s: newHomeGrps){
				//Make connection to the GO node of that group, if you are not the GO of that Group.
				if(!s.equals(NewInfo.getGinfo().getDeviceName())){
					if(myDeviceList.getByName(s)!=null){

						nws.connect(myDeviceList.getByName(s).getVirtIp(), 10001, NewInfo.getGinfo().getDeviceName(),s);
						Log.v("NearTweat:","Making connection to the GO="+s+".From the Device="+NewInfo.getGinfo().getDeviceName());
					}
				}
			}

			for(String s: disConnectedHomeGrps){

				/*
				 * Case 1: You may have left the group. Or the GO may have left the group.
				 * 
				 * If you have left the group ,there will still be an entry in the mGroups of SimWifiP2pInfo.
				 * If the Go had left then there will no entry in the mGroups.Then you need to wait till you get message from the GO,
				 * which contains the state information.Then you receive Network_Membership_Changed Event where you connect to the new GO.
				 * The new GO will save the state and starts accepting the connections.					  
				 */
				if(NewInfo.getGinfo().getmGroups().containsKey(s)){
					//Means , you left the group.So you can disconnect to the GO.
					
					if(nws.getGoConns().containsKey(s)){
						nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
					}
				}
				else{
					//Go left the group.

					if(nws.getGoConns().containsKey(s)){
						nws.getGoConns().get(s).kill(false);//Kill without waiting for the StateInformation Message from the GO.
						//
					}
					
				}

			}
		}

		myGroupInfo.mergeUpdate(NewInfo.getGinfo());

		Log.v("NearTweat","MyServiceHandler: Handling NetWorkMemberrShipChanged.Request of peerList Completed");
		
	}
	
	
	public void connect(String virtIp, int i, String deviceName,String Go) {
		nws.connect(virtIp, i, deviceName,Go);

	}
	public void setServer(NearTweatServer nearTweatServer) {
		this.server = nearTweatServer;

	}
	public void setIamGo(boolean b) {
		this.iamGo = b;

	}
	public boolean isIamGo() {
		return this.iamGo;

	}
	public NearTweatServer getServer() {
		return this.server;
	}
}
class groupListener implements GroupInfoListener {
	MyServiceHandler mService;
	public groupListener(MyServiceHandler myServiceHandler){
		this.mService = myServiceHandler;
	}
	public void onGroupInfoAvailable(SimWifiP2pDeviceList arg0,
			SimWifiP2pInfo arg1) {
		/*Copy the Received info to my Info */
		Log.v("NearTweat","GroupInfoListener Method Execution Started.");
		mService.myGroupInfo.mergeUpdate(arg1);
		mService.myDeviceList.mergeUpdate(arg0);

		SimWifiP2pInfo mInfo = arg1;
		//mInfo.getDeviceName();. This is the GroupName.GroupName is the GO deviceName.
		if(mInfo.askIsGO()){
			if(mService.isIamGo()){
				/*I am already GO . nothing to do */
			}
			else{
				mService.setIamGo(true);
				/* I have to start the server and handle all the incoming connections */
				mService.setServer(new NearTweatServer());
				mService.getServer().connect();
				/*I connect to the Server I have hosted */
				if(mService.myDeviceList.getByName(mService.myGroupInfo.getDeviceName())!=null){
					mService.connect(mService.myDeviceList.getByName(mService.myGroupInfo.getDeviceName()).getVirtIp(), 10001, mService.myGroupInfo.getDeviceName(),mService.myGroupInfo.getDeviceName());
				}
				Log.v("NewarTweat:","Successfully Started server from NetworkService as I am the GO");

			}
		}
		else{
			if(mService.isIamGo()){
				/*I am already GO . I have to cleanUP. */
				mService.setIamGo(false);
				mService.getServer().cleanUp();
			}
			else{
				/*I am not a GO. Nothing to do */

			}
		}
		mService.groupInfoRequestDone=true;
		Log.v("NearTweat","GroupInfoListener Method Execution Complete.");
	}
}

class peerListener implements PeerListListener{

	MyServiceHandler mService;
	SimWifiP2pInfo gInfo;
	public peerListener(MyServiceHandler myServiceHandler){
		this.mService = myServiceHandler;
	}
	public void setInfo(SimWifiP2pInfo obj) {
		this.gInfo = obj;

	}
	@Override
	public void onPeersAvailable(SimWifiP2pDeviceList peers) {
		Log.v("NearTweat","OnPeerAvailable Method execution Started");
		mService.myDeviceList.mergeUpdate(peers);
		/*Compare for change in the Groups in which this node is memeber of */
		/*if(mService.myGroupInfo.getmHomeGroups().containsAll(gInfo.getmHomeGroups())){
			//donothing.
		}
		else{
			//Some new Group was created and this device is member of that group.Make Connection to the GO of that group.
			Set<String> newGrp = new TreeSet<String>(gInfo.getmHomeGroups());
			Log.v("NearTweat","My Exisiting groups are "+mService.myGroupInfo.getmHomeGroups());
			Log.v("NearTweat","My New Groups are "+newGrp);
			newGrp.removeAll(mService.myGroupInfo.getmHomeGroups());
			for(String s: newGrp){
				//Make connection to the GO node of that group.
				if(!s.equals(mService.myGroupInfo.getDeviceName())){
					if(mService.myDeviceList.getByName(s)!=null){

						mService.nws.connect(mService.myDeviceList.getByName(s).getVirtIp(), 10001,gInfo.getDeviceName());
						Log.v("NearTweat:","Making connection to the GO="+s+".From the Device="+gInfo.getDeviceName());
					}
				}


			}
			//Merge the list.
			mService.myGroupInfo.getmHomeGroups().addAll(gInfo.getmHomeGroups());

			mService.peerListRequestDone = true;

		}
		Log.v("NearTweat","OnPeerAvailable Method execution Completed");*/
	}

}

class MemeberShipListener implements MemberShipChangeListener{

	@Override
	public void onMemberShipChange(SimWifiP2pDeviceList devices,
			SimWifiP2pInfo groupInfo) {
		// TODO Auto-generated method stub

	}

}


