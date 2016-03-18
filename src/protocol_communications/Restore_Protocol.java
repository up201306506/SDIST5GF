package protocol_communications;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import file_utils.ChunkKey;
import file_utils.FileManager;
import file_utils.ProtocolEnum;
import network_communications.M_Socket;

public class Restore_Protocol extends Protocol {

	private static String _HEAD = "GETCHUNK ";
	private static String _REPLY_HEAD = "CHUNK";

	private static int _MAX_NUMBER_OF_RETRIES = 5;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds
	
	private Thread getChunkResponseThread;
	private volatile Boolean getChunkResponseActive = false;
	
	public Restore_Protocol(FileManager fm, Map<ChunkKey, Integer> cs, M_Socket mc, M_Socket mdr) {
		super(fm, cs, mc);
		this.mdr = mdr;
				
	}
	
	//Requesting Chunk Request  [X] --> GetChunk ---> [ ]
	//							[X] <--   Chunk  <--- [ ]
	public Boolean sendGetChunkRequest(String version, String senderId, String fileId, int chunkNum){
		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		
		final boolean getchunkComplete = false;
		
		String msg = _HEAD + version + " " + senderId + " " + fileId + " " + chunkNum;
		mc.send(msg);
		
		
		//
		System.out.println("sent GETCHUNK");
			
		
		return false;
	}
	
	
	
	//Receiving Chunk Request   [ ] --> GetChunk ---> [X]
	//							[ ] <--   Chunk  <--- [X]
	public boolean startGetChunkResponse(){
		
		if(!getChunkResponseActive)
		{
			getChunkResponseThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (getChunkResponseActive) {
						getChunkResponseLogic();
					}
				}
			});
			getChunkResponseThread.start();
			getChunkResponseActive = true;
			return true;
		}

		return false;
	}
	
	public boolean stopGetChunkResponse(){
		if(getChunkResponseActive)
		{
			getChunkResponseActive = false;
			try {
				getChunkResponseThread.join();
			} catch (InterruptedException e) {e.printStackTrace();}
			
		}
		
		return false;
	}
	
	public boolean getChunkResponseLogic(){
			
		String msg = mc.receive(ProtocolEnum.GETCHUNK);
		
		if(msg == null)
			return false;
		
		System.out.println("Recebi GETCHUNK");
		mdr.send("CHUNK test test test");
		
		
		return true;
	}
}
