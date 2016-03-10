package protocol_communications;

import java.util.Map;

import file_management.Chunk;
import network_communications.M_Socket;

public class Backup_Protocol extends Protocol {
	
	private static String _HEAD = "PUTCHUNK";
	
	private Map<ChunkKey, Integer> stored;
	
	public Backup_Protocol(M_Socket mc, M_Socket mdb) {
		super(mc);
		this.mdb = mdb;
	}
	
	public boolean sendPutChunck(String version, String senderId, String fileId, int chunkNum, int replicationDegree, Chunk chunk) {
		String chunkData = chunk.getData();
		if(chunkData == null) return false;
		
		mc.send(_HEAD + " " + version + " " + senderId + " " + fileId + " " + chunkNum + " " + replicationDegree +
				_CRLF + _CRLF + chunkData);
		
		stored.put(new ChunkKey(fileId, chunkNum), replicationDegree);
		
		return true;
	}

}
