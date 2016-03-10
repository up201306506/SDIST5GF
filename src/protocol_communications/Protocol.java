package protocol_communications;

import network_communications.M_Socket;

public abstract class Protocol {
	
	public static String _CRLF;
	
	public class ChunkKey{
		private String _fileId;
		private int _chunkNum;
		
		public ChunkKey(String fId, int cNum){
			_fileId = fId;
			_chunkNum = cNum;
		}
	}
	
	public M_Socket mc;
	public M_Socket mdr;
	public M_Socket mdb;

	public Protocol(M_Socket mc){
		
		byte[] crlfArray = new byte[2];
		crlfArray[0] = 0xD;
		crlfArray[1] = 0xA;
		_CRLF = new String(crlfArray);
		
		this.mc = mc;
	}
}
