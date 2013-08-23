package com.ist.neartweat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.ist.neartweat.SecondActivity.MyHandler;
import com.ist.neartweat.messages.Message;
import com.ist.neartweat.messages.ReplyMessage;
import com.ist.neartweat.messages.SpamMessage;
import com.ist.neartweat.platForm.Reply;
import com.ist.neartweat.platForm.ReplyComparator;
import com.ist.neartweat.platForm.Tweat;
import com.ist.neartweat.service.NetworkService;
import com.ist.neartweat.service.NetworkService.NetBinder;

public class TweatDetailsActivity extends Activity {
	
	NetworkService mService;
	TweatDetailsActivity myself = this;
	boolean mBound = false;
	Tweat t;
	ArrayList<Reply> pubReplies;
	Hashtable<String, ArrayList<Reply>> pvtreplies;
	ArrayList<String> pubStrings;
	ArrayList<String> pvtStrings=  new ArrayList<String>();
	ArrayList<String> allStrings = new ArrayList<String>();
	String ClientId;
	ArrayAdapter<String> replyAdapter;
	MyHandler hdlr ;
	boolean readLoop = true;
	long timeStamp;//Used to get the ReplyMessages from the Service.
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweat_details);
		Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		
		Intent it = getIntent();
	     t = (Tweat) it.getSerializableExtra("com.ist.neartweat.Tweat");
	     this.ClientId = it.getStringExtra("com.ist.neartweat.ClientID");
	     this.timeStamp  = it.getLongExtra("com.ist.neartweat.TweatDetailsTimeStamp",System.currentTimeMillis());
	     pubReplies = t.getPubliReplies();
	     if(t.getPvtReplies().size()!=0){
	    	 pvtreplies = t.getPvtReplies();
	     }
	     else{
	    	 pvtreplies = null;
	     }
	     
	     pubStrings = new ArrayList<String>();
	     for(Reply r : pubReplies){
	    	 pubStrings.add(r.getSrc()+"->"+r.getContent());
	     }
	     
	     if(pvtreplies!=null){
	    	 for(String s: pvtreplies.keySet()){
	    		 ArrayList<Reply> al = pvtreplies.get(s);
	    		 if(al.size()!=0){
	    			 //Sort the replies based on time
	    			 Collections.sort(al,new ReplyComparator());
	    			 for(Reply r: al){
	    				 pvtStrings.add("PVT:FROM->"+r.getSrc()+"->"+r.getContent());
	    			 }
	    		 }
		    	 
		     }
	     }
	     allStrings.add("The tweat Id is "+t.getTweatId());
	     allStrings.addAll(pubStrings);
	     allStrings.addAll(pvtStrings);
	     
	     ListView replyListView = (ListView) findViewById(R.id.repliesID);
	     replyAdapter = new ArrayAdapter<String>(this,R.layout.replyitem ,allStrings);
	     replyListView.setAdapter(replyAdapter);
	     hdlr= new MyHandler();		
			startThread(hdlr);
	     
	}
	protected void onResume(){

		hdlr.post(new Runnable(){
			public void run(){
				replyAdapter.notifyDataSetChanged();
			}
		});
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweat_details, menu);
		return true;
	}
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			System.out.println("Connected to the service from Third activity");
			NetBinder binder = (NetBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	
	public void startAttachment(View v){
		System.out.println("Attachment creation Started");
		Intent intent = new Intent(myself, Attachment.class);
		intent.putExtra("com.ist.neartweat.ClientID",ClientId);
		startActivity(intent);
		System.out.println("Attachment creation ended");
	}
	
	public void sendReply(View v){
		System.out.println("sendReply started");
		EditText editText = (EditText) findViewById(R.id.replyTextID);
        String replyStr = editText.getText().toString();
        boolean isPrivate = false;
        editText.clearComposingText();
        /*Reply Format is pvt%dest_ClientId%replyString
         * or
         * pub%replyString
         */
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
        
        if (checkBox.isChecked()){
        	isPrivate = true;
            checkBox.setChecked(false);

        }

        String[] replySubStr =  replyStr.split("%");
       try {
    	   if(isPrivate){
    		   mService.sendMsg(new ReplyMessage(new Reply(this.ClientId,replySubStr[0],replySubStr[1],System.currentTimeMillis()),t.getTweatId(),isPrivate));
    	   }
    	   else{
    		   /*Since this is a public reply the destination is ALL*/
    		   mService.sendMsg(new ReplyMessage(new Reply(this.ClientId,"ALL",replyStr,System.currentTimeMillis()),t.getTweatId(),isPrivate));
    	   }
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		System.out.println("Exception while sending reply");
		e.printStackTrace();
	}
        
		System.out.println("sendReply started ended");
	}
	
	public void sendSpam(View v){
		System.out.println("sendSpam Started");
		EditText editText = (EditText) findViewById(R.id.spamTextID);
		/*
		 * If user wants to mark the tweet as Spam then he has to type Tweat%<tweatid> else
		 * he has to type REPLY%<fromClientId..which is displayed before -> >
		 */
        String replyStr = "REPLY%"+editText.getText().toString();///User has to enter the TweatId or .
       try {
		mService.sendMsg(new SpamMessage(this.ClientId,replyStr));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		System.out.println("Exception while sending SpamMessage");
		e.printStackTrace();
	}
		System.out.println("sendSpam ended");
	}
	
	public void startThread(final Handler hdlr){
		System.out.println("New Thread Start");
		new Thread(){
			public void run(){
				while(!mBound){

				}
				System.out.println("From Thread. Now reading Reply Messages Since I(TweatDetails Activity) connected to Service");
				while(readLoop){
					ArrayList<ReplyMessage> al= mService.giveReplyMsgs(t.getTweatId(),timeStamp);
					timeStamp = System.currentTimeMillis();//Update the timeStamp. Next time you will receive after this time only.
					if(al!=null){
						System.out.println("Received Reply message"+al.size());
						for(Message m : al){
							handleReplyMessage(m,hdlr);
						}
						
					}

					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("readLoop set to false. So I(TweatDetails Activity)  stop reading messages from Service");
			}
		}.start();
		System.out.println("New Thread Start completed");
	}
	protected void handleReplyMessage(Message m, Handler hdlr) {
		// TODO Auto-generated method stub
		if(m instanceof ReplyMessage){
			ReplyMessage rm = (ReplyMessage)m;
			if(rm.isPrivate()){
				//if(pvtreplies.containsKey(rm.getR().getSrc())){
					//pvtreplies.get(rm.getR().getSrc()).add(rm.getR());
					String s = "PVT:FROM->"+rm.getR().getSrc()+"->"+rm.getR().getContent();
					pvtStrings.add(s);
					allStrings.add(s);
				//}
				//else{
					
				//}
			}
			else{
				String s= rm.getR().getSrc()+"->"+rm.getR().getContent();
				pvtStrings.add(s);
				allStrings.add(s);
			}
			
			
			hdlr.post(new Runnable(){
				public void run(){
					replyAdapter.notifyDataSetChanged();
				}
			});
		}
		
	}
/*
	@Override
	public void onBackPressed (){
		readLoop =false;
		mService.unbindService(mConnection);
	finish();
	}*/
	static class MyHandler extends Handler{
		public void HandleMessage(){

		}
	}
	
}


