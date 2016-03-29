package Test;

import java.io.IOException;
import java.util.Map;

import file_utils.FileManager;
import file_utils.ReplicationValue;
import file_utils.StoreChunkKey;
import network_communications.M_Socket;
import protocol_communications.Deletion_Protocol;

public class Test_Protocol_Deletion_Receive {

	public static void main(String[] args) {

		FileManager fm = new FileManager();

		Map<String, String> fileNames = fm.readFileIdToName();
		Map<StoreChunkKey, ReplicationValue> chunkStored = fm.readStoreChunkReplicationRegisters();

		M_Socket mc = new M_Socket("224.224.224.224", 15000);

		Deletion_Protocol dp = new Deletion_Protocol(fm, fileNames, chunkStored, mc, "este_pc");

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
