package Test;

import java.util.HashMap;
import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;

public class Test_Protocol_Backup_Send {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		Map<ChunkKey, Integer> chunkStored = new HashMap<>();
		
		M_Socket mc = new M_Socket("224.224.224.224", 15000);
		M_Socket mdb = new M_Socket("224.224.224.225", 15001);
		
		Backup_Protocol bp = new Backup_Protocol(fm, chunkStored, mc, mdb);
		
		bp.backupFile("PostBox/music.mp3", "2.5", 1);
	}
}