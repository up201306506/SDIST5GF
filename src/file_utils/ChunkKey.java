package file_utils;

public class ChunkKey {
	private String fileId;
	private int chunkNum;
	
	public ChunkKey(String fId, int cNum){
		fileId = fId;
		chunkNum = cNum;
	}
}