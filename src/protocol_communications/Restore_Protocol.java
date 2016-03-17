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
		
		final boolean getchunkComplete = false;
		
		
		while(numOfTries <= _MAX_NUMBER_OF_RETRIES && !getchunkComplete){
			
			mc.send(_HEAD + " " + version + " " + senderId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF);
			
/*
			ExecutorService executor = Executors.newSingleThreadExecutor();
			try {
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						
						while(true){
							String data = mc.receive(ProtocolEnum.STORED);
							if(data != null){
								
								System.out.println("stored");
								
								// CHUNK  Part
								int blankSpaceIndex = data.indexOf(" ");
								String holder = data.substring(0, blankSpaceIndex);								
								if(holder.equals(_REPLY_HEAD))
								{
									//getchunkComplete = true;
									return;
								}
							}
						}
					}
				}).get(waitInterval, TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				System.out.println("TIMEOUT");
				numOfTries++;
				waitInterval = waitInterval * 2;
			}
			executor.shutdown();
*/	
			
		}
		
		
		
		
		
		return false;
	}
	
	
	
	//Receiving Chunk Request    [ ] --> Chunk ---> [X]
	public Boolean receiveGetChunkRequest(){
		return false;
	}
	
}
