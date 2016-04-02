package Test;

import java.util.Map;

import file_utils.FileManager;
import file_utils.ReplicationValue;
import file_utils.StoreChunkKey;
import network_communications.M_Socket;
import protocol_communications.Deletion_Protocol;

public class Test_Protocol_Deletion_Send {
	
	public static void main(String[] args) {
		
		String thisPeerId = "estePc";

		FileManager fm = new FileManager(thisPeerId);

		Map<String, String> fileNames = fm.readFileIdToName();
		Map<StoreChunkKey, ReplicationValue> chunkStored = fm.readStoreChunkReplicationRegisters();

		M_Socket mc = new M_Socket("224.224.224.224", 15000);

		Deletion_Protocol dp = new Deletion_Protocol(fm, fileNames, chunkStored, mc, thisPeerId);

		System.out.println("Deleting chunks backup");
		
		dp.sendDeletionChunk("nomeFicheiro", "1.0");

		System.exit(0);
	}
}
