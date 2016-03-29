package protocol_communications;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public class Reclaim_Protocol extends Protocol {
	
	private static String _HEAD = "REMOVED";

	private Thread receiveRemovedThread;

	public Reclaim_Protocol(FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, M_Socket mcSocket, String peerId) {
		super(fm, fIfN, cs, mcSocket, peerId);
	}
}
