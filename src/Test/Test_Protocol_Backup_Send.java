package Test;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;

public class Test_Protocol_Backup_Send {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		Map<String, String> fileNames = FileManager.readFileIdToName();
		Map<StoreChunkKey, ReplicationValue> chunkStored = FileManager.readStoreChunkReplicationRegisters();
		
		M_Socket mc = new M_Socket("224.224.224.224", 15000);
		M_Socket mdb = new M_Socket("224.224.224.225", 15001);
		
		Backup_Protocol bp = new Backup_Protocol(fm, fileNames, chunkStored, mc, mdb);
		
		bp.backupFile("PostBox/a.png", "2.8", 1);
		
		FileManager.writeFileIdToNameRegisters(fileNames);
		FileManager.writeStoreChunkReplicationRegisters(chunkStored);
		
		System.exit(0);
	}
}