package protocol_communications;

import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;

public abstract class Protocol {

	public static String _CRLF;
	
	public Map<ChunkKey, Integer> chunkStored;

	public M_Socket mc;
	public M_Socket mdr;
	public M_Socket mdb;

	public FileManager fm;

	public Protocol(FileManager fm, Map<ChunkKey, Integer> cs, M_Socket mc){

		this.fm = fm;
		chunkStored = cs;

		byte[] crlfArray = new byte[2];
		crlfArray[0] = 0xD;
		crlfArray[1] = 0xA;
		_CRLF = new String(crlfArray);

		this.mc = mc;
	}
}
