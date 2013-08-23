package com.ist.neartweat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.ist.neartweat.messages.Message;
import com.ist.neartweat.messages.NewPollMessage;
import com.ist.neartweat.messages.PollAnswerMessage;
import com.ist.neartweat.messages.PollListMessage;
import com.ist.neartweat.messages.PollResultMessage;
import com.ist.neartweat.platForm.Option;
import com.ist.neartweat.platForm.Poll;
import com.ist.neartweat.service.NetworkService;
import com.ist.neartweat.service.NetworkService.NetBinder;

public class PollActivity extends Activity {
	NetworkService mService;
	boolean mBound = false;
	MyHandler hdlr ;
	boolean readLoop=true;
	String ClientId;
	ArrayAdapter<String> pollRequestsAdapter;
	ArrayAdapter<String> pollResultAdapter;
	ArrayList<String> pollRequestStrings = new ArrayList<String>();
	ArrayList<String>	pollResultStrings = new ArrayList<String>();	
	Hashtable<String,NewPollMessage> pollRequestMsgs =  new Hashtable<String,NewPollMessage>();
	Hashtable<String,PollResultMessage> pollResultMsgs =  new Hashtable<String,PollResultMessage>();
	PollListMessage polListMsg =null;
	boolean invokePollRequestAdapter =false;
	boolean invokePollResultAdapter =false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll);
		Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		Intent it = getIntent();

		this.ClientId = it.getStringExtra("com.ist.neartweat.ClientID");
		pollRequestStrings.add("POLL REQUESTS LIST");
		pollResultStrings.add("POLL ANSWERS LIST");
		
		ListView pollRequestListView = (ListView) findViewById(R.id.pollRequestsListView);
		ListView pollResultListView = (ListView) findViewById(R.id.pollResultsListView);

		pollRequestsAdapter = new ArrayAdapter<String>(this,R.layout.poll_request ,pollRequestStrings);
		pollRequestListView.setAdapter(pollRequestsAdapter);

		pollResultAdapter = new ArrayAdapter<String>(this,R.layout.poll_result ,pollResultStrings);
		pollResultListView.setAdapter(pollResultAdapter);
		
		hdlr= new MyHandler();		
		startThread(hdlr);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.poll, menu);
		return true;
	}
	
	/*@Override
	public void onBackPressed (){
		readLoop =false;
		mService.unbindService(mConnection);
		finish();
	}*/
	
	protected void onResume(){
		hdlr.post(new Runnable(){
			public void run(){
				pollRequestsAdapter.notifyDataSetChanged();
			}
		});
		
		hdlr.post(new Runnable(){
			public void run(){
				pollResultAdapter.notifyDataSetChanged();
			}
		});
		
		super.onResume();
		
	}

	public void sendPollAnswer(View v){
		EditText editText = (EditText) findViewById(R.id.pollAnswerText);
		String pollAnswer = editText.getText().toString();
		String[] sarr  = pollAnswer.split(":");
		String pollId = sarr[0];
		int option = Integer.parseInt(sarr[1]);
		PollAnswerMessage pam = new PollAnswerMessage(this.ClientId,pollId,option);
		try {
			mService.sendMsg(pam);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception while sending PollAnswerMessage");
			e.printStackTrace();
		}

	}

	public void sendNewPoll(View v){
		/*
		 * poll_string%1:<optionString>%2:OPtionString....
		 */
		EditText editText = (EditText) findViewById(R.id.newPollText);
		String pollStr = editText.getText().toString();
		String[] pollSubStr =  pollStr.split("%");

		Poll newPoll = new Poll(this.ClientId,pollSubStr[0]);
		int i=0;String[] sarr;
		for(i=1;i<pollSubStr.length;i++){
			sarr =  pollSubStr[i].split(":");
			newPoll.addOption(new Option(Integer.parseInt(sarr[0]),sarr[1]));
		}
		NewPollMessage plm = new NewPollMessage(this.ClientId,newPoll);
		try {
			mService.sendMsg(plm);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception while sending NewPollMessage");
			e.printStackTrace();
		}
	}
	/*This thread reads messages from the Network Service */
	public void startThread(final Handler hdlr){
		System.out.println("New Thread Start");
		new Thread(){
			public void run(){
				while(!mBound){

				}
				System.out.println("From Thread. Now reading Messages Since Service connected from Polls Activity");
				polListMsg = mService.getPollListMsg();
				handlePolListMsg(polListMsg,hdlr);
				
				while(readLoop){
					ArrayList<Message> al= mService.givePollMsgs();
					invokePollRequestAdapter =false;
					invokePollResultAdapter=false;
					if(al!=null){
						//System.out.println("2nd Act received message"+al.size());
						for(Message m : al){
							handleMessage(m,hdlr);
						}
						
						if(invokePollRequestAdapter){
							hdlr.post(new Runnable(){
								public void run(){
									System.out.println("Notifying the pollRequestAdapter");
									pollRequestsAdapter.notifyDataSetChanged();
								}
							});
							
						}
						
						if(invokePollResultAdapter){
							hdlr.post(new Runnable(){
								public void run(){
									System.out.println("Notifying the pollResultAdapter");
									pollResultAdapter.notifyDataSetChanged();
								}
							});
							
						}
					}

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		System.out.println("New Thread Start completed");
	}
	
	public void handlePolListMsg(PollListMessage msg,Handler hdlr){
		System.out.println("Received PollListMessage");
		//pollRequestStrings = new ArrayList<String>();
		//pollRequestMsgs = new Hashtable<String,NewPollMessage>();
		ArrayList<Poll> polls =  msg.getPolls();
		for(Poll p : polls){
			String s = "POLL ID is:"+p.getPollId()+".Options are:";
			ArrayList<Option> options = p.getOptions();
			int i=0;
			for(i=0;i<options.size();i++){
				s = s+"Option "+options.get(i).getOprionNumber()+" is :"+ options.get(i).getOptionText()+"$$";
			}
			
			pollRequestStrings.add(s);
		}

		hdlr.post(new Runnable(){
			public void run(){
				pollRequestsAdapter.notifyDataSetChanged();
			}
		});
		
	}
	
	
	public void handleMessage(Message msg, Handler hdlr){
		if(msg instanceof NewPollMessage){
			System.out.println("Received New Poll Message");
			NewPollMessage npm = (NewPollMessage)msg;
			if(pollRequestMsgs.containsKey(npm.getPoll().getPollId())){
				//do nothing
				//invokePollRequestAdapter = true;
			}
			else{
				pollRequestMsgs.put(npm.getPoll().getPollId(),npm);
				String s = "POLL ID is:"+npm.getPoll().getPollId()+".POLL STRING="+npm.getPoll().getPollString()+".\nOptions are:\n";
				ArrayList<Option> options = npm.getPoll().getOptions();
				int i=0;
				for(i=0;i<options.size();i++){
					s = s+"Option "+options.get(i).getOprionNumber()+" is :"+ options.get(i).getOptionText()+".\n";
				}
				System.out.println("Received New Poll Message is "+s);
				
				pollRequestStrings.add(s);
				invokePollRequestAdapter = true;
			}
		}
		else if(msg instanceof PollResultMessage){
			System.out.println("Received PollResultMessage");
			PollResultMessage prm = (PollResultMessage)msg;
			if(pollResultMsgs.containsKey(prm.getPollId())){
				//Do nothing
				//invokePollResultAdapter = true;
			}
			else{
				pollResultMsgs.put(prm.getPollId(), prm);
				/*String s = "POLL ID is:"+prm.getPoll().getPollId()+". Options and the results are:\n";
				ArrayList<Option> options = prm.getPoll().getOptions();
				int i=0;
				for(i=0;i<options.size();i++){
					s = s+"Option "+options.get(i).getOprionNumber()+" is :"+ options.get(i).getOptionText()+"VoteCount="+ options.get(i).getVoteCount()+".\n";
				}*/
				
				pollResultStrings.add(prm.getPollResultMessage());
				invokePollResultAdapter = true;
			}
		}
	}
	
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			System.out.println("Connected to the service from second activity");
			NetBinder binder = (NetBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	static class MyHandler extends Handler{
		public void HandleMessage(){

		}
	}
}
