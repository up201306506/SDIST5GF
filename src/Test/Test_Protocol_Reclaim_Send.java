package Test;

import java.util.Map;

import file_utils.FileManager;
import file_utils.ReplicationValue;
import file_utils.StoreChunkKey;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;
import protocol_communications.Reclaim_Protocol;

public class Test_Protocol_Reclaim_Send {

	public static void main(String[] args) {
		
		String thisPeerId = "este_pc";
		long spaceToReclaim = 10000; // bytes

		FileManager fm = new FileManager(thisPeerId);

		Map<String, String> fileNames = fm.readFileIdToName();
		Map<StoreChunkKey, ReplicationValue> chunkStored = fm.readStoreChunkReplicationRegisters();

		M_Socket mc = new M_Socket("224.224.224.224", 15000);
		M_Socket mdb = new M_Socket("224.224.224.225", 15001);
		
		Backup_Protocol bp = new Backup_Protocol(fm, fileNames, chunkStored, mc, mdb, thisPeerId);
		Reclaim_Protocol rp = new Reclaim_Protocol(fm, fileNames, chunkStored, mc, thisPeerId, bp);

		System.out.println("Reclaiming disk space");
		
		System.out.println(rp.reclaimSpace(spaceToReclaim));
		
		System.out.println("Reclaimed " + spaceToReclaim + " bytes");

		System.exit(0);
	}
}