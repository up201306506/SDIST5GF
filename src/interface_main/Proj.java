package interface_main;

import java.util.HashMap;
import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;

public class Proj {

	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		// FileId -> FileName
		Map<String, String> fileNames = fm.readFileIdToName();
		// FileId + version + ChunkNum -- > replicationDegree + numOfChunksStored
		Map<StoreChunkKey, ReplicationValue> chunkStored = new HashMap<>();

		M_Socket mc = new M_Socket("224.225.226.227", 12345);
		M_Socket mdb = new M_Socket("224.225.226.228", 12346);
		
		Backup_Protocol bp = new Backup_Protocol(fm, fileNames, chunkStored, mc, mdb, "este_pc");
		
		// Closing execution functions
		fm.writeFileIdToNameRegisters(fileNames);
		
		System.exit(0);
	}
}