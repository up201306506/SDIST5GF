package file_utils;

public class StoreChunkKey {
	private String fileId;
	private String version;
	private int chunkNum;
	
	public StoreChunkKey(String fId, String v, int cNum){
		fileId = fId;
		version = v;
		chunkNum = cNum;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof StoreChunkKey)){
	        return false;
	    }else{
	    	StoreChunkKey that = (StoreChunkKey)obj;
	    	
	    	if(!this.fileId.equals(that.fileId)) return false;
	    	if(!this.version.equals(that.version)) return false;
	    	if(this.chunkNum != that.chunkNum) return false;
	    	
	    	return true;
	    }
	}
	
	@Override
	public int hashCode() {
		int hash = this.fileId.hashCode();
	    hash = hash * 31 + this.version.hashCode();
	    hash = hash * 31 + ("" + this.chunkNum).hashCode();
	    return hash;
	}
}