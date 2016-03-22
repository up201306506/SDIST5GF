package protocol_communications;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public abstract class Protocol {

	public static String _CRLF;
	
	public Map<String, String> fileIdToFileName;
	public Map<StoreChunkKey, ReplicationValue> chunksStored;

	public M_Socket mc;
	public M_Socket mdr;
	public M_Socket mdb;

	public FileManager fm;

	public Protocol(FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, M_Socket mc){
		this.fm = fm;
		fileIdToFileName = fIfN;
		chunksStored = cs;

		byte[] crlfArray = new byte[2];
		crlfArray[0] = 0xD;
		crlfArray[1] = 0xA;
		_CRLF = new String(crlfArray);

		this.mc = mc;
	}
}
