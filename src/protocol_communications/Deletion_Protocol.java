package protocol_communications;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public class Deletion_Protocol extends Protocol {
	public Deletion_Protocol(FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, M_Socket mc) {
		super(fm, fIfN, cs, mc);
		// TODO Auto-generated constructor stub
	}

	//Send Deletion Request
	public Boolean sendDeletionRequest(){
		
		return false;
	}
}
