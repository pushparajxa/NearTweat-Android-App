package com.ist.neartweat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ist.neartweat.messages.Message;
import com.ist.neartweat.messages.NewTweatMessage;
import com.ist.neartweat.messages.NewTweatUpdateMessage;
import com.ist.neartweat.messages.PrivateMessage;
import com.ist.neartweat.messages.RegistartionSuccessMessage;
import com.ist.neartweat.messages.ReplyMessage;
import com.ist.neartweat.messages.SpamMessage;
import com.ist.neartweat.platForm.Tweat;
import com.ist.neartweat.service.NetworkService;
import com.ist.neartweat.service.NetworkService.NetBinder;

public class SecondActivity extends Activity {
	public static final int Reply = 0;
	public static final int Spam = 1;
	NetworkService mService;
	boolean mBound = false;
	ArrayList<Message> msgs = new ArrayList<Message>();
	boolean readLoop = true;
	ArrayList<Tweat> tweats=null ;
	ArrayList<tweatRow> tweatStrings=new ArrayList<tweatRow>();
	ArrayAdapter<tweatRow> tweatAdapter;
	String ClientId;
	SecondActivity myself = this;
	MyHandler hdlr ;
	MyUiHandler uiHdlr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);
		Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		Intent it  = getIntent();
		this.ClientId= it.getStringExtra("com.ist.nearTweat.ClientId");
		
		hdlr= new MyHandler();		
				startThread(hdlr);
				uiHdlr = new MyUiHandler(this);
	}
	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		System.out.println("2nd Act");
		/*Start the thread to read messages from the NetworkService */


		System.out.println("2nd Act Thd Ext");

		ListView tweatListView = (ListView) findViewById(R.id.tweatViewID);
		//ArrayList<String> tweatStrings1 = new ArrayList<String>();tweatStrings1.add("item1");tweatStrings1.add("item2");tweatStrings1.add("item3");
		tweatAdapter = new CustomListViewAdapter(this,R.layout.customtweatitem ,tweatStrings,this.uiHdlr);

		tweatListView.setAdapter(tweatAdapter);
		//tweatStrings.add("New String added");
		//tweatAdapter.notifyDataSetChanged();

	}
	
	protected void onReusme(){
		hdlr.post(new Runnable(){
			public void run(){
				tweatAdapter.notifyDataSetChanged();
			}
		});
		 super.onResume();
	}
	
	public void sendTweat(View v){
		/*
		 * Create a new TweatMessage class..send that Message to the server. Server will send the tweat to all other client.
		 * To send message call mService.sendMsg(msg)..The tweat owner id can be fetched by calling this.ClientId
		 * 
		 */
		EditText editText = (EditText) findViewById(R.id.tweatID);
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
	public void startAttachment(View v){
		System.out.println("Attachment creation Started");
		Intent intent = new Intent(this, Attachment.class);
		intent.putExtra("com.ist.neartweat.ClientID",ClientId);
		startActivity(intent);
		System.out.println("Attachment creation ended");
	}
	public void sendSpam(String obj){
		System.out.println("sendSpam Started");
		
		/*
		 * If user wants to mark the tweet as Spam then he has to type Tweat%<tweatid> else
		 * he has to type REPLY%<fromClientId..which is displayed before -> >
		 */
        String replyStr = "Tweat%"+obj;///User has to enter the TweatId or .
       try {
		mService.sendMsg(new SpamMessage(this.ClientId,replyStr));
	} catch (IOException e) {
		// TODO Auto-generated catch block
		System.out.println("Exception while sending SpamMessage");
		e.printStackTrace();
	}
		System.out.println("sendSpam ended");
	}

	public void startThird(String str){
		System.out.println("OnItemListnere Started");
		
		String tweatId = str;
		Intent intent = new Intent(myself, TweatDetailsActivity.class);
		Tweat t = getTweatWithId(tweatId);
		intent.putExtra("com.ist.neartweat.TweatDetailsTimeStamp",System.currentTimeMillis());
		intent.putExtra("com.ist.neartweat.ClientID",ClientId);
		intent.putExtra("com.ist.neartweat.Tweat", t);
		startActivity(intent);
		System.out.println("OnItemListnere creation ended");
	}

	public void startPollActivity(View v){
		System.out.println("PollActivity creation Started");
		Intent intent = new Intent(myself, PollActivity.class);
		intent.putExtra("com.ist.neartweat.ClientID", ClientId);
		startActivity(intent);
		System.out.println("PollActivity creation ended");
	}

	public void startTweatActivity(View v){
		System.out.println("TweatActivity creation Started");
		Intent intent = new Intent(myself, TweatActivity.class);
		intent.putExtra("com.ist.neartweat.ClientID", ClientId);
		startActivity(intent);
		System.out.println("TweatActivity creation ended");
	}

	public class itemListener implements OnItemClickListener{
		SecondActivity secondActivity;
		public itemListener(SecondActivity secondActivity) {
			this.secondActivity = secondActivity;
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int arg2,
				long arg3) {

			Intent intent = new Intent(this.secondActivity, TweatDetailsActivity.class);
			Tweat t = getTweatWithId(((TextView) v).getText().toString());
			intent.putExtra("com.ist.neartweat.ClientID",ClientId);
			intent.putExtra("com.ist.neartweat.Tweat", t);
			startActivity(intent);
		}

	}


	/*This thread reads messages from the Network Service */
	public void startThread(final Handler hdlr){
		System.out.println("New Thread Start");
		new Thread(){
			public void run(){
				while(!mBound){

				}
				System.out.println("From Thread. Now reading Messages Since Service connected");
				while(readLoop){
					ArrayList<Message> al= mService.giveMsgs();
					if(al!=null){
						System.out.println("2nd Act received message"+al.size());
						for(Message m : al){
							handleMessage(m,hdlr);
						}
						msgs.addAll(al);
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
	
	@Override
	public void onBackPressed (){
		readLoop =false;
		mService.unbindService(mConnection);
	finish();
	}

	public Tweat getTweatWithId(String text) {
		for(Tweat t :tweats){
			if(t.getTweatId().equals(text)){
				return t;
			}
		}
		return null;
	}
	public void handleMessage(Message msg, Handler hdlr){
		if(msg instanceof RegistartionSuccessMessage){
			System.out.println("Received RegistrationSuccess Message");
			RegistartionSuccessMessage rms = (RegistartionSuccessMessage)msg;
			this.tweats = rms.getTweats();
			System.out.println("The received tweat size is "+rms.getTweats().size());
			try{
				for(Tweat t: tweats){
					if(t.isPhoto()){
						this.tweatStrings.add(new tweatRow(t.getTweatId()+"->"+t.getTweatString(),t.getImage()));
						System.out.println("Tweat String ::"+t.getTweatId()+"->"+t.getTweatString());
					}
					else{
						this.tweatStrings.add(new tweatRow(t.getTweatId()+"->"+t.getTweatString()));
						System.out.println("Tweat String ::"+t.getTweatId()+"->"+t.getTweatString());
					}
					
				}

				hdlr.post(new Runnable(){
					public void run(){
						tweatAdapter.notifyDataSetChanged();
					}
				});
			}
			catch(Exception e){
				System.out.println("Exception in RegistrationSuccess");
				e.printStackTrace();
			}

		}
		else if(msg instanceof ReplyMessage){
			ReplyMessage rm = (ReplyMessage)msg;
			String tweatId= rm.getTweatId();
			Tweat twet = getTweatWithId(tweatId);
			if(rm.isPrivate()){
				twet.addPvtReply(rm.getR());
			}
			else{
				twet.addPubReply(rm.getR());
			}
			
		}
		
		else if(msg instanceof NewTweatUpdateMessage){
			NewTweatUpdateMessage ntum = (NewTweatUpdateMessage)msg;
			Tweat t = ntum.getTweat();
			if(this.tweats!=null){
				this.tweats.add(t);
			}
			else{
				this.tweats = new ArrayList<Tweat>();
				this.tweats.add(t);
			}
			if(t.isPhoto()){
				this.tweatStrings.add(new tweatRow(t.getTweatId()+"->"+t.getTweatString(),t.getImage()));
				System.out.println("Tweat String ::"+t.getTweatId()+"->"+t.getTweatString());
			}
			else{
				this.tweatStrings.add(new tweatRow(t.getTweatId()+"->"+t.getTweatString()));
				System.out.println("Tweat String ::"+t.getTweatId()+"->"+t.getTweatString());
			}
			
			//notifiy the tweatAdapter;
			hdlr.post(new Runnable(){
				public void run(){
					tweatAdapter.notifyDataSetChanged();
				}
			});
		}
		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
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

	public void SendMsg(View view){
		try {
			mService.sendMsg(new PrivateMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception while Sendming Message from Second Activity");
			e.printStackTrace();
		}
	}
	static class MyHandler extends Handler{
		public void HandleMessage(){

		}
	}
}


 class tweatRow{
	 private byte[] image;
	 private String str;
	 private boolean  isPhoto;
	 
	 public tweatRow(String str,byte[] image){
		 this.setStr(str);
		 this.setImage(image);
		 setPhoto(true);
	 }
	 public tweatRow(String str){
		 this.setStr(str);
		 setPhoto(false);
	 }
	 
	 
	 public byte[] getImage() {
		return image;
	}
	public void setImage(byte[] image) {
		this.image = image;
		
	}
	
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}
	public boolean isPhoto() {
		return isPhoto;
	}
	public void setPhoto(boolean isPhoto) {
		this.isPhoto = isPhoto;
	}
	 
 }

 class MyUiHandler extends Handler{
	 SecondActivity act;
	 public MyUiHandler(SecondActivity secondActivity) {
		this.act = secondActivity;
	}

	@Override
	 public void handleMessage(android.os.Message msg){
		 switch(msg.what){
		 case SecondActivity.Reply:
			 act.startThird((String)msg.obj);
			 break;
		 case SecondActivity.Spam:
			 System.out.println("hi");
			 act.sendSpam((String)msg.obj);
			 break;
		 }
	 }
 }
 
 class CustomListViewAdapter extends ArrayAdapter<tweatRow> {
	 
	    Context context;
	    MyUiHandler uihdlr;

	 
	    public CustomListViewAdapter(Context context, int resourceId,
	            List<tweatRow> items,MyUiHandler uihdlr) {
	        super(context, resourceId, items);
	        this.context = context;
	        this.uihdlr = uihdlr;
	    }
	    
	 
	    /*private view holder class*/
	    private class ViewHolder {
	        ImageView imageView;
	        TextView txtTitle;
	        Button button; 
	        Button button1;
	       
	    }
	    
	    
	 
	    public View getView(int position, View convertView, ViewGroup parent) {
	        ViewHolder holder = null;
	        tweatRow rowItem = getItem(position);
	 
	        LayoutInflater mInflater = (LayoutInflater) context
	                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.customtweatitem, null);
	            holder = new ViewHolder();
	           
	            
	            holder.txtTitle = (TextView) convertView.findViewById(R.id.text);
	            holder.imageView = (ImageView) convertView.findViewById(R.id.image);
	            holder.button = (Button) convertView.findViewById(R.id.button1);
	            holder.imageView.buildDrawingCache();
	            holder.button1 = (Button) convertView.findViewById(R.id.button2);

	            convertView.setTag(holder);
	            
	           // Bitmap bmap = Bitmap.createBitmap(holder.imageView.getDrawingCache());
	            

	        	
	            
	        } else
	        {
	            holder = (ViewHolder) convertView.getTag();}
	        
	          //you can pass what ever to this class you want,
	          //i mean, you can use array(postion) as per the logic you need to implement 
	       
	  
	        
	       if(rowItem.isPhoto()){
	    	   /*
	    	   File imgFile = new File("/mnt/sdcard/DCIM/Catching.jpg");
	    	   if(imgFile.exists()){
	    		   System.out.println("File Found");
	    	       ByteArrayOutputStream stream = new ByteArrayOutputStream();

	    		   Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	    		   myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
	    	       byte[] byteArray = stream.toByteArray();
	    	      

	    	   }
	    	   else{
	    		   System.out.println("Does not exist");
	    	   }*/
	    	   
	    	   
	    	   holder.txtTitle.setText(rowItem.getStr());
	    	  // Put the code to decompress and disülay bitmap.
	    	  // holder.imageView.setImageResource(rowItem.getImageId());
	    	  // holder.imageView.setImageBitmap();
    	      holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(rowItem.getImage(), 0, rowItem.getImage().length));
    	      
	    	  holder.imageView.setOnClickListener(new imageViewClickListener(position,holder,rowItem.getImage()));
	    	  convertView.setDrawingCacheEnabled(true);
	    	  holder.button.setOnClickListener(new buttonClickListener(rowItem.getStr()));
	    	  holder.button1.setOnClickListener(new buttonClickListener1(rowItem.getStr()));

	       }
	       else{
	    	   holder.txtTitle.setText(rowItem.getStr());
	    	   holder.imageView.setVisibility(View.INVISIBLE);
	    	   holder.txtTitle.setOnClickListener(new textClickListener(position,holder,rowItem.getStr()));
		       holder.button.setOnClickListener(new buttonClickListener(rowItem.getStr()));
		       holder.button1.setOnClickListener(new buttonClickListener1(rowItem.getStr()));
	       }

	       
	    

	       
	        
	 
	        return convertView;
	    }
	    
	    class buttonClickListener implements OnClickListener{
	    	String clientid;
	    	
		       public buttonClickListener(String clientid1){
		    	   this.clientid = clientid1;
		       }
		       
		    	    @Override
		    	    public void onClick(View v) {
		    	    	String[] seperated = clientid.split("-");
		    	    	System.out.println("Hallo"+seperated[0]);
		    	    	android.os.Message msg = uihdlr.obtainMessage(SecondActivity.Reply,seperated[0] );
		    	    	uihdlr.sendMessage(msg);
		    	    
	    }
	    }
	    class buttonClickListener1 implements OnClickListener{
	    	String clientid;
	    	
		       public buttonClickListener1(String clientid1){
		    	   this.clientid = clientid1;
		       }
		       
		    	    @Override
		    	    public void onClick(View v) {
		    	    	String[] seperated = clientid.split("-");
		    	    	android.os.Message msg = uihdlr.obtainMessage(SecondActivity.Spam,seperated[0] );
		    	    	uihdlr.sendMessage(msg);
		    	    
	    }
	    }
		       
		       
	    
	    class textClickListener implements OnClickListener{
	    	int position1;
		    ViewHolder holder;
		    String tweat;
		    public textClickListener( int pos1, ViewHolder hol1, String string)	{
		    	this.position1 = pos1;
		    	this.holder = hol1;
		    	this.tweat = string;
		    	
		    }
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			 	Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			   	sharingIntent.setType("text/plain");
			   	String shareBody = tweat;
			   	sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
			   	sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
			   	context.startActivity(Intent.createChooser(sharingIntent, "Shared Tweat from Neartweat"));
			}
	    } 
	    
	    
	     class imageViewClickListener implements OnClickListener {
		       int position;
		       ViewHolder holder1;
		       byte[] picture;
		       
		        public imageViewClickListener( int pos, ViewHolder hol,byte[] pic)
		            {
		                this.position = pos;
		                this.holder1 = hol;
		                this.picture = pic;
		               		            }
		        public void onClick(View v) 
		        
		        
		            {// you can write the code what happens for the that click and 
		        
			        


	
			        /*v.buildDrawingCache(true);
			        Bitmap bmap = Bitmap.createBitmap(v.getDrawingCache());
			        v.setDrawingCacheEnabled(false);*/
		            System.out.println("Hallo"+position); // you will get the selected row index in position
		            Bitmap bitmap = BitmapFactory.decodeByteArray(picture , 0, picture.length);
		        	
		            String root = Environment.getExternalStorageDirectory().toString();
		            File myDir = new File(root + "/saved_images");    
		            myDir.mkdirs();	
		            File file = new File (myDir, "image1.png");
		            if (file.exists ()) file.delete (); 
		            try {
		                   FileOutputStream out = new FileOutputStream(file);
		                   bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		                   out.flush();
		                   out.close();

		            } catch (Exception e) {
		                   e.printStackTrace();
		            }
		            String external = Environment.getExternalStorageDirectory().toString();
		            String path = "file://" + external + "/saved_images/image1.png";
			        //String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
			    	  Uri uri = Uri.parse(path);
			        Intent share = new Intent(Intent.ACTION_SEND);
			    	share.setType("image/png");
			    	share.putExtra(Intent.EXTRA_STREAM, uri);
			    	
			    	context.startActivity(Intent.createChooser(share, "Share Image"));

			    	

		           }
		       }
	     
	}

