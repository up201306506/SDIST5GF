package protocol_communications;

import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;

public class Deletion_Protocol extends Protocol {

	public Deletion_Protocol(FileManager fm, Map<ChunkKey, Integer> cs, M_Socket mc) {
		super(fm, cs, mc);
		// TODO Auto-generated constructor stub
	}

	//Send Deletion Request
	public Boolean sendDeletionRequest(){
		
		return false;
	}
	
}
