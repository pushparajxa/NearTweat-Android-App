package com.ist.neartweat.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import android.util.Log;

import com.ist.neartweat.messages.BroadCastMessage;
import com.ist.neartweat.messages.ByeMessage;
import com.ist.neartweat.messages.Message;
import com.ist.neartweat.messages.NewPollMessage;
import com.ist.neartweat.messages.NewTweatMessage;
import com.ist.neartweat.messages.NewTweatUpdateMessage;
import com.ist.neartweat.messages.PollAnswerMessage;
import com.ist.neartweat.messages.PollListMessage;
import com.ist.neartweat.messages.PollResultMessage;
import com.ist.neartweat.messages.PrivateMessage;
import com.ist.neartweat.messages.RegistartionSuccessMessage;
import com.ist.neartweat.messages.RegistrationMessage;
import com.ist.neartweat.messages.ReplyMessage;
import com.ist.neartweat.messages.SpamMessage;
import com.ist.neartweat.messages.StateInformationMessage;
import com.ist.neartweat.platForm.Poll;
import com.ist.neartweat.platForm.Reply;
import com.ist.neartweat.platForm.ServerState;
import com.ist.neartweat.platForm.Tweat;
import com.ist.neartweat.wifidirect.sockets.SimWifiP2pSocket;
import com.ist.neartweat.wifidirect.sockets.SimWifiP2pSocketServer;

public class NearTweatServer{
	private Hashtable<String,SimWifiP2pSocket> clientHandles =  new Hashtable<String,SimWifiP2pSocket>();
	private Hashtable<String,ClientHandlerThread> clientThreads = new Hashtable<String,ClientHandlerThread>();
	//Tweat Owner and list of tweats made by that user. Tweat Ids will start with <clientId>+1.
	private Hashtable<String,ArrayList<Tweat>> tweats = new  Hashtable<String,ArrayList<Tweat>>();
	private Hashtable<String,ObjectOutputStream> clientOutStreams = new Hashtable<String,ObjectOutputStream>();
	private Hashtable<String,Integer> spamAccusationCount = new Hashtable<String,Integer>();
	private Hashtable<String,ArrayList<String>> accusedAndReporters = new Hashtable<String,ArrayList<String>>();
	private int SPAM_LIMIT=2;
	private static  ArrayList<String> spammers = new ArrayList<String>();
	private Hashtable<String,ArrayList<String>> pollIDandAnsweredClients = new Hashtable<String,ArrayList<String>>();
	private Hashtable<String,Poll> polls = new Hashtable<String,Poll>(); 
	private Hashtable<String,Integer> clientAndPollsCreatedCnt = new Hashtable<String,Integer>();
	private Hashtable<String,Poll> completedPolls = new Hashtable<String,Poll>();
	
	SimWifiP2pSocketServer serverSocket;
	ServerThread serverThread;

	public NearTweatServer(ServerState state) {
		
		this.accusedAndReporters = state.getAccusedAndReporters();
		this.clientAndPollsCreatedCnt = state.getClientAndPollsCreatedCnt();
		this.completedPolls = state.getCompletedPolls();
		this.pollIDandAnsweredClients = state.getPollIDandAnsweredClients();
		this.polls = state.getPolls();
		this.spamAccusationCount =state.getSpamAccusationCount();
		this.tweats = state.getTweats();
		spammers = state.getSpammers();
	}
	
	public NearTweatServer(){
		
	}
	
	public static boolean checkIamSpammer(String clientId){
		//Log.v("NearTweat","Checkimg spam status for client"+clientId);
		if(clientId ==null )
			return false;
		else if(spammers.contains(clientId))
			return true;
		else
			return false;
	}
	public  void connect( ){
		try {
			int i=0;

			serverSocket = new SimWifiP2pSocketServer(10001);
			serverThread = new ServerThread(this, serverSocket);
			serverThread.start();

			Log.v("NearTweat","Succefully created the server");
			/*For Testing */
			ArrayList<Tweat> test1 = new ArrayList<Tweat>();
			ArrayList<Tweat> test2 = new ArrayList<Tweat>();
			for(i=0;i<15;i++){
				test1.add(new Tweat("raj","raj:"+i,"TweatString from tweatId"+i));
				test2.add(new Tweat("max","max:"+i,"TweatString from tweatId"+i));

			}
			ArrayList<Tweat> test3 = new ArrayList<Tweat>();
			Tweat ty = new Tweat("raj2","raj2:1","TweatString from tweatId raj2");
			ty.addPubReply(new Reply("raj","raj2","This is fisrt reply",System.currentTimeMillis()));
			ty.addPubReply(new Reply("raj","raj2","This is second reply",System.currentTimeMillis()));
			test3.add(ty);
			//tweatServer.tweats.put("raj",test1);
			//tweatServer.tweats.put("max",test2);
			//tweatServer.tweats.put("raj2",test3);
			/* End of for testing */



		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("NearTweat","Failed to create the server at port 10001");
			e.printStackTrace();
		}
	}

	public Boolean addClient(String clientId, SimWifiP2pSocket clientSocket) {
		if(clientHandles.containsKey(clientId)){
			return false;
		}
		else{
			clientHandles.put(clientId, clientSocket);
			return true;
		}
	}

	public void addOutStream(String clientId, ObjectOutputStream ops){
		clientOutStreams.put(clientId,ops);
	}


	public void handleMessage(Message msg) throws CloneNotSupportedException {
		if(msg instanceof PrivateMessage){

		}
		else if(msg instanceof BroadCastMessage){

		}
		else if(msg instanceof ByeMessage){

		}
		else if(msg instanceof NewTweatMessage){
			/*NewTweatMessage ntm = (NewTweatMessage)msg;
			String creator = ntm.getTweatCreator();
			String tweatString = ntm.getTweatString();
			Tweat t;
			if(tweats.containsKey(creator)){
				int twc= tweats.get(creator).size()+1;
				t= new Tweat(creator,creator+twc,tweatString);
				tweats.get(creator).add(t);

			}
			else{
				ArrayList<Tweat> al = new ArrayList<Tweat>();
				t = new Tweat(creator,creator+1,tweatString);
				al.add(t);
				tweats.put(creator, al);

			}

			NewTweatUpdateMessage ntum = new NewTweatUpdateMessage(t);
			/* Send NewTweatUpdateMessage  to 
			 * every client ,even to the creator of the tweat since he  doesn't know the tweatId while sending it to the server.
			 */
			/*for(String s: this.clientOutStreams.keySet()){

				try {
					this.clientOutStreams.get(s).writeObject(ntum);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("NearTweat","Exception while sending NewTweatUpdateMessage to the Client"+s);
					e.printStackTrace();
				}



			}*/

			/*
			 * New Code which can handlw tweats with photos.
			 */
			NewTweatMessage ntm = (NewTweatMessage)msg;
			String creator = ntm.getTweatCreator();
			String tweatString = ntm.getTweatString();
			Tweat t;
			if(tweats.containsKey(creator)){
				int twc= tweats.get(creator).size()+1;
				t= new Tweat(creator,creator+twc,tweatString);
				if(ntm.isIs_photo()){
					t.setIsPhoto(true);
					t.setImage(ntm.getPhoto());
				}
				else{
					t.setIsPhoto(false);
				}
				tweats.get(creator).add(t);

			}
			else{
				ArrayList<Tweat> al = new ArrayList<Tweat>();
				t = new Tweat(creator,creator+1,tweatString);
				if(ntm.isIs_photo()){
					t.setIsPhoto(true);
					t.setImage(ntm.getPhoto());
				}
				else{
					t.setIsPhoto(false);
				}
				al.add(t);
				tweats.put(creator, al);

			}

			NewTweatUpdateMessage ntum = new NewTweatUpdateMessage(t);
			/* Send NewTweatUpdateMessage  to 
			 * every client ,even to the creator of the tweat since he  doesn't know the tweatId while sending it to the server.
			 */
			for(String s: this.clientOutStreams.keySet()){

				try {
					this.clientOutStreams.get(s).writeObject(ntum);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Exception while sending NewTweatUpdateMessage to the Client"+s);
					e.printStackTrace();
				}



			}


		}
		else if(msg instanceof RegistrationMessage){
			//We will come here only when registration is successful in ClientHandlerThread class's run method.
			//send RegistrationSuccessMessage which contains tweats
			String clientId = ((RegistrationMessage)msg).getClientId();
			RegistartionSuccessMessage rms = new RegistartionSuccessMessage();
			ArrayList<Tweat> toSend = new ArrayList<Tweat>();
			for(String s: tweats.keySet()){
				for(Tweat t : tweats.get(s)){
					toSend.add(t.clone(clientId));
				}
			}
			rms.setTweats(toSend);
			try {
				clientOutStreams.get(clientId).writeObject(rms);
				Log.v("NearTweat","Sending the tweats" + "tweat size is "+rms.getTweats().size());
				/*Send polls */
				PollListMessage pollist = new PollListMessage();
				ArrayList<Poll> pols = new ArrayList<Poll>();
				for(String s: polls.keySet()){
					pols.add(polls.get(s));
				}

				pollist.addPolls(pols);
				clientOutStreams.get(clientId).writeObject(pollist);
				Log.v("NearTweat","Sending the polls after successful registration to the client"+clientId);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		else if(msg instanceof ReplyMessage){
			ReplyMessage rm = (ReplyMessage)msg;
			String srcClient = rm.getR().getSrc();
			Log.v("NearTweat","Received reply message from "+srcClient);
			//Update the GlobalTweat 
			Tweat t = getTweatWithId(rm.getTweatId());
			if(rm.isPrivate()){
				t.addPvtReply(rm.getR());
			}
			else{
				t.addPubReply(rm.getR());
			}

			//Send the Reply Message to other possible  live Clients.
			if(rm.isPrivate()){
				try {
					//We are sending the reply to dest .At source it was already added.
					//After receiving this message it will update.
					//clientOutStreams.get(rm.getR().getSrc()).writeObject(rm);
					clientOutStreams.get(rm.getR().getDest()).writeObject(rm);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("NearTweat","Exception while sending Private reply to Source and Destination");
					e.printStackTrace();
				}

			}
			else{
				//Since this is a public Reply message , send it to all the clients. except Source
				for(String s: this.clientOutStreams.keySet()){
					if(!rm.getR().getSrc().equals(s)){
						try {
							this.clientOutStreams.get(s).writeObject(rm);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							Log.v("NearTweat","Exception while sending reply Message to the Client"+s);
							e.printStackTrace();
						}
					}


				}

			}


		}
		else if(msg instanceof SpamMessage){

			SpamMessage sm= (SpamMessage)msg;
			String reporter = sm.getReporterId();
			String contents = sm.getReportString();
			String[] contentArr =  contents.split("%");

			if(contentArr[0].equalsIgnoreCase("TWEAT")){
				String tweatOwner = getOwnerOfTweatId(contentArr[1]);
				Log.v("NearTweat","Received spam accusation against client "+ tweatOwner+"from client "+ reporter+"Accsation count="+spamAccusationCount.get(tweatOwner));
				if(spamAccusationCount.containsKey(tweatOwner)){

					ArrayList<String> reporters = accusedAndReporters.get(tweatOwner);
					if(reporters.contains(reporter)){
						//Do nothing.. You already reported him as spammer.
						return;
					}
					else{
						reporters.add(tweatOwner);
						int cnt = spamAccusationCount.get(tweatOwner)+1;
						if(cnt>=SPAM_LIMIT){
							//Mark this client as Spammer.
							if(spammers.contains(tweatOwner)){
								//do nothing. Already marked as spammer.
							}
							else{
								spammers.add(tweatOwner);
							}
						}
						else{
							spamAccusationCount.put(tweatOwner,new Integer(cnt));

						}
					}


				}

				else{
					spamAccusationCount.put(tweatOwner,new Integer(1));
					accusedAndReporters.put(tweatOwner,new ArrayList<String>());
				}
			}

			else{
				//REPLY
				String clientId = contentArr[1]; //contentArr[0]  is "REPLY"
				Log.v("NearTweat","Received spam accusation against client "+ clientId+"from client "+ reporter);
				if(spamAccusationCount.containsKey(clientId)){

					ArrayList<String> reporters = accusedAndReporters.get(clientId);
					if(reporters.contains(reporter)){
						//Do nothing.. You already reported him as spammer.
						return;
					}
					else{
						reporters.add(clientId);
						int cnt = spamAccusationCount.get(clientId)+1;
						if(cnt>=SPAM_LIMIT){
							//Mark this client as Spammer.
							if(spammers.contains(clientId)){
								//do nothing. Already marked as spammer.
							}
							else{
								spammers.add(clientId);
							}
						}
						else{
							spamAccusationCount.put(clientId,new Integer(cnt));
						}
					}


				}

				else{
					spamAccusationCount.put(clientId,new Integer(1));
					accusedAndReporters.put(clientId,new ArrayList<String>());
				}
			}
		}
		else if(msg instanceof NewPollMessage){

			NewPollMessage npm = (NewPollMessage)msg;
			String pollCreator = npm.getPollOwner();
			Log.v("NearTweat","Received New Poll Message from "+pollCreator);
			Poll pl = npm.getPoll();int cnt;
			if(clientAndPollsCreatedCnt.containsKey(pollCreator)){
				cnt = clientAndPollsCreatedCnt.get(pollCreator)+1;
				pl.setPollId(pollCreator+cnt);
				clientAndPollsCreatedCnt.put(pollCreator, cnt+1);
			}
			else{
				cnt=1;
				clientAndPollsCreatedCnt.put(pollCreator, cnt);
				pl.setPollId(pollCreator+cnt);
			}

			polls.put(pollCreator+cnt,pl);
			//send the NewPollMessage to  live clients
			for(String s: this.clientOutStreams.keySet()){
				//if(!s.equals(pollCreator)){
				try {
					this.clientOutStreams.get(s).writeObject(npm);
					Log.v("NearTweat","Sending New poll message to "+s);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("NearTweat","Exception while sending NewPollMessage to the Client"+s);
					e.printStackTrace();
				}
				//}

			}
		}
		else if(msg instanceof PollAnswerMessage){
			PollAnswerMessage pam = (PollAnswerMessage)msg;
			String answeredClient = pam.getVoterId();
			Log.v("NearTweat","Receive Poll Answer message from "+answeredClient);
			String pollId = pam.getPollId();
			if(pollIDandAnsweredClients.containsKey(pollId)){
				if(pollIDandAnsweredClients.get(pollId).contains(answeredClient)){
					//do nothing..this client already answered the poll.
				}
				else{
					Poll pl = polls.get(pollId);
					int cnt =pl.getOption(pam.getVoteOption()).getVoteCount();
					pl.getOption(pam.getVoteOption()).setVoteCount(cnt+1);
					//add the client to answered poll list.
					pollIDandAnsweredClients.get(pollId).add(answeredClient);
				}

			}
			else{
				pollIDandAnsweredClients.put(pollId, new ArrayList<String>());
				Poll pl = polls.get(pollId);
				int cnt =pl.getOption(pam.getVoteOption()).getVoteCount();
				pl.getOption(pam.getVoteOption()).setVoteCount(cnt+1);
				//add the client to answered poll list.
				pollIDandAnsweredClients.get(pollId).add(answeredClient);

			}

			//Need to send the results to the clients.
			if(pollIDandAnsweredClients.get(pollId).size()==clientHandles.size()-1){
				//Every one answered.The poll owner will not answer.
				//Add the poll to completedPolls list.
				completedPolls.put(pollId, polls.get(pollId));


				String ResultString = "Poll Id:"+pollId+"\n.Poll String is:"+polls.get(pollId).getPollString()+"\n"+polls.get(pollId).toString();

				PollResultMessage prm = new PollResultMessage(pollId,polls.get(pollId),ResultString);


				Log.v("NearTweat","Sending PollResultMessage for poll Id"+pollId+"Results are "+polls.get(pollId).toString());

				/* Send to all clients including owner of the poll */
				for(String s: this.clientOutStreams.keySet()){

					try {
						this.clientOutStreams.get(s).writeObject(prm);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.v("NearTweat","Exception while sending Poll Answer Message to the Client"+s);
						e.printStackTrace();
					}
				}
				//Removing from the global polls list.
				//polls.remove(pollId);

			}

		}

	}

	public String getOwnerOfTweatId(String s){
		Set<String> owners = this.tweats.keySet();
		for(String own: owners){
			for(Tweat t : tweats.get(own)){
				if(t.getTweatId().equals(s))
					return own;
			}
		}
		return null;
	}
	public Tweat getTweatWithId(String s){
		Set<String> owners = this.tweats.keySet();
		for(String own: owners){
			for(Tweat t : tweats.get(own)){
				if(t.getTweatId().equals(s))
					return t;
			}
		}
		return null;
	}
	public void cleanUp() {
		/*Need to some work like packing the things and sending them to the new GO*/
		serverThread.close();
		for(String s: this.clientThreads.keySet()){
			Log.v("NearTweat","Removing the Client "+s);
			clientThreads.get(s).cleanUp();
			clientThreads.remove(s);
			if(this.clientHandles.containsKey(s)){
				try {
					clientHandles.get(s).close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				clientHandles.remove(s);
			}
		Log.v("NearTweat","Successfully Removed the Client "+s);
		}
		
		
	}
	public void removeClient(String s) {
		
		if(this.clientThreads.containsKey(s)){
			Log.v("NearTweat","Removing the Client "+s);
			clientThreads.get(s).cleanUp();
			clientThreads.remove(s);
			if(this.clientHandles.containsKey(s)){
				try {
					clientHandles.get(s).close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				clientHandles.remove(s);
			}
		Log.v("NearTweat","Successfully Removed the Client "+s);
		}
	}
	
	public void addClientThread(String clientId,
			ClientHandlerThread clientHandlerThread) {
		if(clientId!=null && clientHandlerThread!=null ){
			this.clientThreads.put(clientId, clientHandlerThread);
		}
	}
	public ArrayList<String> sendStateInformation(String Go) {
		ArrayList<String> list = new ArrayList<String>();
		
		/*Populate the Server State Object and send to the StateInformationMessage to all the clients */
				
		ServerState state = new ServerState();
		
		state.setAccusedAndReporters(this.accusedAndReporters);
		state.setClientAndPollsCreatedCnt(this.clientAndPollsCreatedCnt);
		state.setCompletedPolls(this.completedPolls);
		state.setPollIDandAnsweredClients(this.pollIDandAnsweredClients);
		state.setPolls(this.polls);
		state.setSpamAccusationCount(this.spamAccusationCount);
		state.setTweats(this.tweats);
		state.setSpammers(spammers);
		
		
		StateInformationMessage stm = new StateInformationMessage(Go,state);
		
		for(String s: this.clientOutStreams.keySet()){
			
			try {
				Log.v("NearTweat","Sent StateInformationMessage to the Client="+s);
				this.clientOutStreams.get(s).writeObject(stm);
				list.add(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.v("NearTweat","Exception while Sending StateInformation to the Client"+s);
				e.printStackTrace();
			}
		}
		
		return list;
		
	}
}

class ServerThread extends Thread{
	NearTweatServer server;
	SimWifiP2pSocketServer serverSocket;
	boolean acceptConnections =true;
	public ServerThread(NearTweatServer server, SimWifiP2pSocketServer serverSocket){
		this.server = server;
		this.serverSocket = serverSocket;
	}
	public void run(){
		SimWifiP2pSocket clientSocket;
		//Accept Connections
		while(acceptConnections){
			try {
				clientSocket = serverSocket.accept();
				new ClientHandlerThread(server,clientSocket).start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	public void close(){
		try {
			acceptConnections = false;
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}