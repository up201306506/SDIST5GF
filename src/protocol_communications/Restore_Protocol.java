package protocol_communications;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ProtocolEnum;
import file_utils.RandomDelay;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public class Restore_Protocol extends Protocol {

	private static String _HEAD = "GETCHUNK";
	private static String _REPLY_HEAD = "CHUNK";

	private Thread receiveGetChunkThread;
	private volatile boolean _sendingRequest;

	public Restore_Protocol(final FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, final M_Socket mc, final M_Socket mdr, String peerId) {
		super(fm, fIfN, cs, mc, peerId);
		this.mdr = mdr;
		
		_sendingRequest = false;

		receiveGetChunkThread = new Thread(new Runnable() {
			public void run() {	
				while(true){
					byte[] data = null;
					do{
						data = mc.receive(ProtocolEnum.GETCHUNK);
					}while(data == null);

					String[] message = M_Socket.getMessage(data);
					if(message == null || message.length != 5) continue;

					// GETCHUNK
					if(!message[0].equals("GETCHUNK")) continue;

					// Version of the getchunk received
					String getChunkVersionReceived = message[1];

					// Id of the GETCHUNK sender
					String getChunkSenderId = message[2];
					if(getChunkSenderId.equals(thisPeerId)) continue;

					// Id of the chunk file to restore
					String getChunkFileId = message[3];

					// Num of the chunk file to restore
					int numOfChunkToRestore = Integer.parseInt(message[4]);

					// Verifies if it is a received chunk
					if(!chunksStored.containsKey(new StoreChunkKey(getChunkFileId, getChunkVersionReceived, numOfChunkToRestore)))
						continue;

					try {
						Thread.sleep(RandomDelay.randomInt(0, 400));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					boolean sendMessage = true;
					while(!_sendingRequest){
						byte[] dataReceivedMDR = mdr.receive(ProtocolEnum.CHUNK);
						if(dataReceivedMDR == null) break;

						String[] messageReceivedMDR = M_Socket.getMessage(dataReceivedMDR);
						if(messageReceivedMDR == null || messageReceivedMDR.length != 5) continue;

						// CHUNK
						if(!messageReceivedMDR[0].equals("CHUNK")) continue;

						// Version of the chunk received
						String chunkVersionReceivedMDR = messageReceivedMDR[1];
						if(!chunkVersionReceivedMDR.equals(getChunkVersionReceived)) continue;

						// Id of the CHUNK sender
						String chunkSenderIdMDR = messageReceivedMDR[2];
						if(chunkSenderIdMDR.equals(thisPeerId)) continue;

						// Id of the chunk file received
						String chunkFileIdMDR = messageReceivedMDR[3];
						if(!chunkFileIdMDR.equals(getChunkFileId)) continue;

						// Num of the chunk file received
						int numOfChunkReceivedMDR = Integer.parseInt(messageReceivedMDR[4]);
						if(numOfChunkReceivedMDR != numOfChunkToRestore) continue;
						
						sendMessage = false;
						break;
					}
					
					if(!sendMessage) continue;

					byte[] chunkToSendData = fm.readChunkData(getChunkVersionReceived, getChunkFileId, numOfChunkToRestore);

					if(chunkToSendData != null)
						sendChunk(getChunkVersionReceived, getChunkFileId, numOfChunkToRestore, chunkToSendData);
				}
			}
		});

		receiveGetChunkThread.start();
	}

	// Sending Data
	public void sendChunk(String version, String fileId, int chunkNum, byte[] data){
		
		String headMessageToSendStr = _REPLY_HEAD + " " + version + " " + thisPeerId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF;
		byte[] messageToSend = M_Socket.joinMessageToChunk(headMessageToSendStr, data);

		mdr.send(messageToSend);
	}

	public byte[] restoreChunk(final String version, final String fileId, final int chunkNum){
		try{
			String headMessageToSendStr = _HEAD + " " + version + " " + thisPeerId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF;
			byte[] messageToSend = headMessageToSendStr.getBytes();

			mc.send(messageToSend);

			ExecutorService executor = Executors.newSingleThreadExecutor();
			Callable<byte[]> callable = new Callable<byte[]>() {
				@Override
				public byte[] call() throws Exception {
					_sendingRequest = true;
					
					while(true){
						byte[] data = null;
						do{
							data = mdr.receive(ProtocolEnum.CHUNK);
						}while(data == null);

						String[] message = M_Socket.getMessage(data);
						if(message == null || message.length != 5) continue;

						// CHUNK
						if(!message[0].equals("CHUNK")) continue;

						// Version of the chunk received
						String chunkVersionReceived = message[1];
						if(!chunkVersionReceived.equals(version)) continue;

						// Id of the CHUNK sender
						String chunkSenderId = message[2];
						if(chunkSenderId.equals(thisPeerId)) continue;

						// Id of the chunk file received
						String chunkFileId = message[3];
						if(!chunkFileId.equals(fileId)) continue;

						// Num of the chunk file received
						int numOfChunkReceived = Integer.parseInt(message[4]);
						if(numOfChunkReceived != chunkNum) continue;

						byte[] chunkData = M_Socket.getChunkData(data);
						
						_sendingRequest = false;
						return chunkData;
					}
				}
			};

			Future<byte[]> future = executor.submit(callable);

			return future.get();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public boolean restoreFile(String fileName, String version){
		String fileId = null;
		for(Map.Entry<String, String> entry : fileIdToFileName.entrySet()){
			if(entry.getValue().equals(fileName)){
				fileId = entry.getKey();
				break;
			}
		}
		
		if(fileId == null) return false;
		
		int chunkNum = 0;
		byte[] tempData = null;
		do{
			tempData = restoreChunk(version, fileId, chunkNum);
			if(tempData == null) return false;
			
			fm.writeInStoreFolderFile(fileId, chunkNum, tempData);
			chunkNum++;
		}while(tempData.length == FileManager._CHUNK_SIZE);
		
		if(!fm.uniteFile(fileId, fileName)) return false;
		
		fm.deleteFolder(fileId);
		
		return true;
	}
}
