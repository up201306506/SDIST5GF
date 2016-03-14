package interface_main;

import java.util.HashMap;
import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;

public class Proj {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		Map<ChunkKey, Integer> chunkStored = new HashMap<>();
		
		M_Socket mc = new M_Socket("224.225.226.227", 12345);
		M_Socket mdb = new M_Socket("224.225.226.228", 12346);
		
		fm.uniteFile("328af81c5767b162c3a373e346f60a390a9baa9c4c67c4a348c22beab14ca3d");
	}
}