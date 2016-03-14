package Test;

import java.util.HashMap;
import java.util.Map;

import file_utils.ChunkKey;
import file_utils.FileManager;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;

public class Test_Protocol_Backup_Receive {
	
	public static void main(String[] args) {
		
		FileManager fm = new FileManager();
		
		Map<ChunkKey, Integer> chunkStored = new HashMap<>();
		
		M_Socket mc = new M_Socket("224.225.226.230", 12345);
		M_Socket mdb = new M_Socket("224.225.226.231", 12346);
		
		Backup_Protocol bp = new Backup_Protocol(fm, chunkStored, mc, mdb);
		
		//System.out.println("MC Socket: 224.225.226.230");
		//System.out.println("MDB Socket: 224.225.226.231");
		
		while(true)
		{
			bp.receiveChunk();			
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println("Queue Sizes: ");
			//System.out.println("MC:" + mc.queueSize(-1) + " MDB:" + mdb.queueSize(-1));
			//System.out.println("MC:" + mc.queueSize(1) + " MDB:" + mdb.queueSize(1));
			System.out.println("MC:" + mc.queueSize(2) + " MDB:" + mdb.queueSize(2));
		}
		
	}
}