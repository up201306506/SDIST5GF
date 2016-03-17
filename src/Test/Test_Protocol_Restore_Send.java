package Test;

import java.util.HashMap;
import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;
import protocol_communications.Restore_Protocol;

public class Test_Protocol_Restore_Send {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		Map<ChunkKey, Integer> chunkStored = new HashMap<>();
		
		M_Socket mc = new M_Socket("224.225.226.230", 12345);
		M_Socket mdr = new M_Socket("224.225.226.232", 12346);
		
		Restore_Protocol rp = new Restore_Protocol(fm, chunkStored, mc, mdr);
		
		
		
	}
}