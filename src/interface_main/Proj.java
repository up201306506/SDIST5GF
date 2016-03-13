package interface_main;

import java.util.HashMap;
import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;

public class Proj {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		Map<ChunkKey, Integer> chunkStored = new HashMap<>();
		
		M_Socket mc = new M_Socket("224.225.226.227", 12345);
		M_Socket mdb = new M_Socket("224.225.226.228", 12346);
		
		Backup_Protocol bp = new Backup_Protocol(fm, chunkStored, mc, mdb);
		
		bp.backupFile("PostBox/test.jpg", "2.5", 5);
		//bp.receiveChunk();
	}
}