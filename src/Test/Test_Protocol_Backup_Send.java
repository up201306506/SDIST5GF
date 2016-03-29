package Test;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;

public class Test_Protocol_Backup_Send {
	
	public static void main(String[] args) {
		
		String thisPeerId = "este_pc";
		
		FileManager fm = new FileManager(thisPeerId);
		
		Map<String, String> fileNames = fm.readFileIdToName();
		Map<StoreChunkKey, ReplicationValue> chunkStored = fm.readStoreChunkReplicationRegisters();
		
		M_Socket mc = new M_Socket("224.224.224.224", 15000);
		M_Socket mdb = new M_Socket("224.224.224.225", 15001);
		
		Backup_Protocol bp = new Backup_Protocol(fm, fileNames, chunkStored, mc, mdb, thisPeerId);
		
		bp.backupFile("ImagesToSend/a.png", "1.0", 1);
		
		fm.writeFileIdToNameRegisters(fileNames);
		fm.writeStoreChunkReplicationRegisters(chunkStored);
		
		System.exit(0);
	}
}