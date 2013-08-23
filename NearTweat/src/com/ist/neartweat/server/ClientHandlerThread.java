package com.ist.neartweat.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import com.ist.neartweat.messages.Message;
import com.ist.neartweat.messages.RegistrationMessage;
import com.ist.neartweat.wifidirect.sockets.SimWifiP2pSocket;

public class ClientHandlerThread extends Thread{
	NearTweatServer server ;
	SimWifiP2pSocket clientSocket ;
	Boolean isRegistered=false;
	String ClientId=null;
	ObjectInputStream ois =null;
	ObjectOutputStream ops = null;
	Boolean loop;
	public ClientHandlerThread(NearTweatServer server, SimWifiP2pSocket clientSocket){
		this.server = server;
		this.clientSocket = clientSocket;

	}
	
	public void cleanUp(){
		
		try {
			this.loop =false;
			this.ois.close();
			this.ops.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void run(){
		 loop= true;

		try {
			ops = new ObjectOutputStream(clientSocket.getOutputStream());
			ops.flush();
			ois = new ObjectInputStream(clientSocket.getInputStream());

		} catch (IOException e1) {

			e1.printStackTrace();
		}

		while(loop){
			try {
				Message msg = (Message)ois.readObject();
				if(isRegistered && this.ClientId!=null && NearTweatServer.checkIamSpammer(this.ClientId) ){
					//I am marked as spam..I should loop until I get removed from the spammers list.
					System.out.println("\nClient with ID="+this.ClientId+" is marked as Spam.\n");
					//while(NearTweatServer.checkIamSpammer(this.ClientId))
						//;
				}

				else if(msg instanceof RegistrationMessage){
					this.ClientId= ((RegistrationMessage) msg).getClientId();
					
					//put the entry in ClientThreads.
					this.server.addClientThread(this.ClientId,this);
					//end of putting
					
					System.out.println("Received Registration Request Message from client "+((RegistrationMessage) msg).getClientId());
					Boolean status = server.addClient(ClientId,clientSocket);
					/*DEBUG*/			status = true; 
					if(status == true){
						isRegistered = true;
						server.addOutStream(ClientId, ops);
						server.handleMessage(msg);
					}
					else{
						loop=false;//already one ClientHandler for this client exists as client already registered.
					}


				}
				else{
					if(isRegistered == true){
						server.handleMessage(msg);
					}

					else{
						//Send Register First Message to the client. Or this client has hacked the system.
						//Can be implemented later.
					}

				}

			} catch (StreamCorruptedException e) {
				System.out.println("Exception in ClientHandlerThread of Client = "+this.ClientId);
				loop =false;
				e.printStackTrace();
			} catch (IOException e) {
				loop =false;
				System.out.println("Exception in ClientHandlerThread of Client = "+this.ClientId);
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				loop =false;
				System.out.println("Exception in ClientHandlerThread of Client = "+this.ClientId);
				e.printStackTrace();
			} catch (CloneNotSupportedException e) {
				loop =false;
				e.printStackTrace();
			}
		}
	}
}
