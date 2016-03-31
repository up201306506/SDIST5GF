package protocol_communications;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ProtocolEnum;
import file_utils.ReplicationValue;
import network_communications.M_Socket;
import peer_main.Peer;

public class Reclaim_Protocol extends Protocol {

	private static String _HEAD = "REMOVED";
	
	private Backup_Protocol reclaimBP;
	
	private Thread receiveRemovedThread;

	public Reclaim_Protocol(final FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, M_Socket mcSocket, String peerId, Backup_Protocol bp) {
		super(fm, fIfN, cs, mcSocket, peerId);
		
		reclaimBP = bp;
		
		receiveRemovedThread = new Thread(new Runnable() {
			public void run() {
				while(true){
					byte[] data = null;
					do{
						data = mc.receive(ProtocolEnum.REMOVED);
					}while(data == null);

					String[] message = M_Socket.getMessage(data);
					if(message == null || message.length != 5) continue;

					// REMOVED
					if(!message[0].equals(_HEAD)) continue;

					// Version of the chunk removed
					String chunkVersionReceived = message[1];

					// Id of the REMOVED sender
					String deleteSenderId = message[2];
					if(deleteSenderId.equals(thisPeerId)) continue;

					// Id of the chunk file removed
					String chunkFileId = message[3];
					
					// Num of the chunk file removed
					int chunkNumReceived = Integer.parseInt(message[4]);

					// Decrements the chunk replicationValue if it exists
					if(!chunksStored.containsKey(new StoreChunkKey(chunkFileId, chunkVersionReceived, chunkNumReceived))) continue;
					
					for(Map.Entry<StoreChunkKey, ReplicationValue> entry : chunksStored.entrySet()){
						if(entry.getKey().equals(new StoreChunkKey(chunkFileId, chunkVersionReceived, chunkNumReceived))){
							entry.getValue().decrementReplicationValue();
							fm.writeStoreChunkReplicationRegisters(chunksStored);
							continue;
						}
					}
				}
			}
		});
		
		receiveRemovedThread.start();
	}
	
	public boolean reclaimSpace(long bytesToReclaim){
		while(fm.getFreeDiskSpace() < Peer.maxDiskSpace || bytesToReclaim > 0){
			Iterator<Entry<StoreChunkKey, ReplicationValue>> it = chunksStored.entrySet().iterator();
			
			if(!it.hasNext()) return false;
			
			Entry<StoreChunkKey, ReplicationValue> entry = it.next();
			
			File chunkFile = new File(fm.storeFolder(entry.getKey().getId()) + File.separator +
										entry.getKey().getId() + "-" + String.format("%06d", entry.getKey().getChunkNum()));
			
			fm.setFreeDiskSpace(fm.getFreeDiskSpace() + chunkFile.length());
			fm.deleteInstance(chunkFile);
			
			sendRemovedChunk(entry.getKey().getId(), entry.getKey().getVersion(), entry.getKey().getChunkNum());
			
			it.remove();
		}
		
		return true;
	}

	// Sending Data
	public void sendRemovedChunk(String fileId, String version, int chunkNum){
		String headMessageToSendStr = _HEAD + " " + version + " " + thisPeerId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF;
		byte[] messageToSend = headMessageToSendStr.getBytes();

		mc.send(messageToSend);
	}
}
