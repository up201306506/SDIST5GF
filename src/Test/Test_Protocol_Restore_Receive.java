package Test;

import java.io.IOException;
import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;
import protocol_communications.Restore_Protocol;

public class Test_Protocol_Restore_Receive {

	public static void main(String[] args) {
		
		String thisPeerId = "este_pc";

		FileManager fm = new FileManager(thisPeerId);

		Map<String, String> fileNames = fm.readFileIdToName();
		Map<StoreChunkKey, ReplicationValue> chunkStored = fm.readStoreChunkReplicationRegisters();

		M_Socket mc = new M_Socket("224.224.224.224", 15000);
		M_Socket mdr = new M_Socket("224.224.224.226", 15002);

		Restore_Protocol rp = new Restore_Protocol(fm, fileNames, chunkStored, mc, mdr, thisPeerId);

		System.out.println("Started receiving...");
		System.out.println("< press any key to stop executing >");

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Stopping!");

		System.exit(0);
	}
}