package protocol_communications;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public class Removal_Protocol extends Protocol {

	public Removal_Protocol(FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, M_Socket mc) {
		super(fm, fIfN, cs, mc);
		// TODO Auto-generated constructor stub
	}

	
	//Send Removal Notification
	public Boolean sendRemovalNotification(){
		
		return false;
	}
	
	//Receive Removal Notification
	public Boolean receiveRemovalNotification(){
		
		return false;
	}
}
