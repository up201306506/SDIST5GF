package protocol_communications;

import java.util.Map;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ProtocolEnum;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public class Deletion_Protocol extends Protocol {
	
	private static String _HEAD = "DELETE";

	private Thread receiveDeleteThread;
	
	public Deletion_Protocol(final FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, M_Socket mcSocket, String peerId) {
		super(fm, fIfN, cs, mcSocket, peerId);
		
		receiveDeleteThread = new Thread(new Runnable() {
			public void run() {
				while(true){
					byte[] data = null;
					do{
						data = mc.receive(ProtocolEnum.DELETE);
					}while(data == null);
					
					String[] message = M_Socket.getMessage(data);
					if(message == null || message.length != 4) continue;

					// DELETE
					if(!message[0].equals("DELETE")) continue;

					// Version of the chunk received
					String chunkVersionReceived = message[1];

					// Id of the PUTCHUNK sender
					String deleteSenderId = message[2];
					if(deleteSenderId.equals(thisPeerId)) continue;

					// Id of the chunk file to store
					String chunkFileId = message[3];
					
					// Deletes the chunk if it exists
					boolean chunkExists = false;
					for(Map.Entry<StoreChunkKey, ReplicationValue> entry : chunksStored.entrySet()){
						if(entry.getKey().getId().equals(chunkFileId) && entry.getKey().getVersion().equals(chunkVersionReceived)){
							chunksStored.remove(entry.getKey()); // May throw ConcurrentModificationException							
							chunkExists = true;
						}
					}
					
					if(!chunkExists) continue;
					
					fm.writeStoreChunkReplicationRegisters(chunksStored);
					fm.deleteFolder(chunkFileId);
				}
			}
		});
		
		receiveDeleteThread.start();
	}

	// Sending Data
	public void sendDeletionChunk(String version, String fileId){
		String headMessageToSendStr = _HEAD + " " + version + " " + thisPeerId + " " + fileId + " " + _CRLF + _CRLF;
		byte[] messageToSend = headMessageToSendStr.getBytes();
		mc.send(messageToSend);
	}
}
