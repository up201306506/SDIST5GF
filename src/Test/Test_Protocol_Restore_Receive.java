package Test;

import java.util.HashMap;
import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;
import protocol_communications.Restore_Protocol;

public class Test_Protocol_Restore_Receive {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		Map<String, String> fileNames = new HashMap<>();
		Map<StoreChunkKey, ReplicationValue> chunkStored = new HashMap<>();
		M_Socket mc = new M_Socket("224.225.226.230", 12345);
		M_Socket mdr = new M_Socket("224.225.226.232", 12346);
		Restore_Protocol rp = new Restore_Protocol(fm, fileNames, chunkStored, mc, mdr);
		
		System.out.println("Teste do receptor de GETCHUNK e que devolve CHUNK");
		rp.startGetChunkResponse();
		try {Thread.sleep(15000);} 
			catch (InterruptedException e) {e.printStackTrace();}
		rp.stopGetChunkResponse();
		
		System.out.println("End");
		
		return;
	}
}