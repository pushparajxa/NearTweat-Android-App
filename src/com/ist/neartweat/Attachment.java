package com.ist.neartweat;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.ist.neartweat.messages.NewTweatMessage;
import com.ist.neartweat.messages.ReplyMessage;
import com.ist.neartweat.platForm.Reply;
import com.ist.neartweat.platForm.Tweat;
import com.ist.neartweat.service.NetworkService;
import com.ist.neartweat.service.NetworkService.NetBinder;

public class Attachment extends Activity {
	
	  private static final int CAMERA_PIC_REQUEST = 1337;  
	  private static int RESULT_LOAD_IMAGE = 1;

	    private static final int SELECT_PHOTO = 100;
	    public Uri imageuri;
	    public Uri imageuri1;
	    public Bitmap thumbnail;
	    NetworkService mService;
		boolean mBound = false;
		String ClientId;
		Tweat t;

	    public String path;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	Intent intent = new Intent(this, NetworkService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attachment);
        
        Intent it  = getIntent();
		this.ClientId= it.getStringExtra("com.ist.neartweat.ClientID");
	    t = (Tweat) it.getSerializableExtra("com.ist.neartweat.Tweat");

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
    
    private void selectgallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);    
        System.out.println("started gallery");
        }


            
        
        
        public void selectgallerypic(View view) {    	
    		selectgallery();
    	  }
        
        public void takephoto(View view) {
   		 dispatchTakePictureIntent();
   	  }
   	
   	private void dispatchTakePictureIntent() {
   	    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
   	    startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
   	}
      public void onActivityResult(int requestCode, int resultCode, Intent data)
      {
    	  super.onActivityResult(requestCode, resultCode, data);
      if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
          Uri selectedImage = data.getData();
          String[] filePathColumn = { MediaStore.Images.Media.DATA };
          Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
          cursor.moveToFirst();
          int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
          String picturePath = cursor.getString(columnIndex);
          cursor.close();
          ImageView imageView = (ImageView) findViewById(R.id.imageView1);
          imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
          thumbnail = (BitmapFactory.decodeFile(picturePath));
      }  
          if (requestCode == CAMERA_PIC_REQUEST) {
              thumbnail = (Bitmap) data.getExtras().get("data");
              ImageView image = (ImageView) findViewById(R.id.imageView1);  
              image.setImageBitmap(thumbnail); 
              imageuri = data.getData();
              
              // do something  
         
          try {
              FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory()+"Filename");
              
              thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
          } catch (Exception e) {
              e.printStackTrace();
          }
          }
      }
      public void tweatpic(View view) {
    	  ByteArrayOutputStream stream = new ByteArrayOutputStream();
          thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream);
          byte[] byteArray = stream.toByteArray();
         // mService.sendMessage(new PhotoMessage(byteArray));
          NewTweatMessage ntm = new NewTweatMessage(this.ClientId,byteArray);
          ntm.setTweatString("This is a Photo Tweat");
          try {
  			mService.sendMsg(ntm);
  		} catch (IOException e) {
  			// TODO Auto-generated catch block
  			System.out.println("Exception while sending NewTweatMessage from the client="+this.ClientId);
  			e.printStackTrace();
  		}
      }
      public void  tweatpicreply(View view){
    	  ByteArrayOutputStream stream = new ByteArrayOutputStream();
          thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream);
          byte[] byteArray = stream.toByteArray();
          System.out.println(t.getTweatId()+" "+byteArray[0]+" "+this.ClientId);
          Boolean isPrivate = false;

          /*ReplyMessage rm = new ReplyMessage(new Reply(this.ClientId,"ALL","Photo Reply",System.currentTimeMillis()),t.getTweatId(),isPrivate,byteArray);
          try {
    			mService.sendMsg(rm);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			System.out.println("Exception while sending NewTweatMessage from the client="+this.ClientId);
    			e.printStackTrace();
    		}
    	  
    	  */
      }
      
      }


