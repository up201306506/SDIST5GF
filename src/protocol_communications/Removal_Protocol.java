package protocol_communications;

import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;

public class Removal_Protocol extends Protocol {

	public Removal_Protocol(FileManager fm, Map<ChunkKey, Integer> cs, M_Socket mc) {
		super(fm, cs, mc);
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
