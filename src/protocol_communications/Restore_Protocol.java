package protocol_communications;

import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;

public class Restore_Protocol extends Protocol {

	private static String _HEAD = "GETCHUNK";
	private static String _REPLY_HEAD = "CHUNK";

	private static int _MAX_NUMBER_OF_RETRIES = 5;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds
	
	
	
	public Restore_Protocol(FileManager fm, Map<ChunkKey, Integer> cs, M_Socket mc, M_Socket mdr) {
		super(fm, cs, mc);
		this.mdr = mdr;
	}
	
	//Requesting Chunk Request  [X] --> Chunk ---> [ ]
	public Boolean sendGetChunkRequest(String version, String senderId, String fileId, int chunkNum){
		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		
		
		
		
		return false;
	}
	
	public Boolean receiveChunkResponse(){
		return false;
	}
	
	
	//Receiving Chunk Request    [ ] --> Chunk ---> [X]
	public Boolean receiveGetChunkRequest(){
		return false;
	}
	
}
