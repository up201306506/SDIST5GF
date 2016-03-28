package Test;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;
import protocol_communications.Restore_Protocol;

public class Test_Protocol_Restore_Send {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		Map<String, String> fileNames = fm.readFileIdToName();
		Map<StoreChunkKey, ReplicationValue> chunkStored = fm.readStoreChunkReplicationRegisters();
		
		M_Socket mc = new M_Socket("224.224.224.224", 15000);
		M_Socket mdr = new M_Socket("224.224.224.226", 15002);
		
		Restore_Protocol rp = new Restore_Protocol(fm, fileNames, chunkStored, mc, mdr);
		
		System.out.println("Sent GetChunk");
		
		// param -> string version, string fileid, int chunknum
		byte[] test = rp.restoreChunk("1.0", "testfile", 15);
		System.out.println("Received chunk length -> " + test.length);
		
		System.exit(0);
	}
}