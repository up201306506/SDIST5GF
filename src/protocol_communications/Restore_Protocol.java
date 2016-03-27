package protocol_communications;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public class Restore_Protocol extends Protocol {

	private static String _HEAD = "GETCHUNK";
	private static String _REPLY_HEAD = "CHUNK";

	private static int _MAX_NUMBER_OF_RETRIES = 5;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds
	
	private Thread getChunkResponseThread;
	private volatile Boolean getChunkResponseActive = false;
	
	public Restore_Protocol(FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, M_Socket mc, M_Socket mdr) {
		super(fm, fIfN, cs, mc);
		this.mdr = mdr;
				
	}
	
	//Requesting Chunk Request  [X] --> GetChunk ---> [ ]
	//							[X] <--   Chunk  <--- [ ]
	public Boolean sendGetChunkRequest(String version, String fileId, int chunkNum){
		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		
		
		final boolean getchunkComplete = false;
		String senderId = "senderIdError";
		try {senderId = InetAddress.getLocalHost().getHostName();} 
			catch (UnknownHostException e) {e.printStackTrace();}
		
		
		//Enviar a mensagem pelo MC
		String msg = _HEAD + " " + version + " " + senderId + " " + fileId + " " + chunkNum;
		//mc.send(msg);
		
		//DEBUG
		System.out.println(msg);
		
		//Esperar pela resposta pelo MDR
		//
		
		//Guardar o Chunk
		//
		
		
		
		return true;
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
			
		//String msg = mc.receive(ProtocolEnum.GETCHUNK);
		/*if(msg == null)
			return false;*/
		
		//DEBUG
		System.out.println("Recebi GETCHUNK");
		//System.out.println(msg);
		
		//Analisar a mensagem
		/*String[] msgvars = msg.split("\\s+");
		if (!msgvars[0].equals(_HEAD))
			return false;
		
		
		//Se a mensagem recebida pertencer ao próprio, descartar a mensagem.
		String senderId = "FIXME!";
		try {
			senderId = InetAddress.getLocalHost().getHostName();
			if (senderId.matches(msgvars[2]))
			{
				System.out.println("A mensagem recebida veio do próprio.");
				return false;
			}
		} catch (UnknownHostException e) {e.printStackTrace();}
		
		
		
		//Procurar o chunk e mandar de volta por mdr
		//Verificar se o ficheiro existe
		double version = Double.parseDouble(msgvars[1]);
		String fileId = msgvars[3];
		int chunkNum = Integer.parseInt(msgvars[4]);
		
		
		String data = "FIXME!!!";
		//mdr.send(_REPLY_HEAD + " " + version + " " + senderId + " " + fileId + " " + version + " " + version + " " + version + " " + _CRLF + _CRLF + data);
		
		*/
		return true;
	}
}
