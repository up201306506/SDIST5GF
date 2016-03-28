package protocol_communications;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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

	public Restore_Protocol(final FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, final M_Socket mc, final M_Socket mdr) {
		super(fm, fIfN, cs, mc);
		this.mdr = mdr;

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
					String thisSenderId = null;
					try{
						thisSenderId = InetAddress.getLocalHost().getHostName();
						if(getChunkSenderId.equals(thisSenderId)) continue;

					} catch (UnknownHostException e) {
						e.printStackTrace();
					}

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
		try{
			String thisSenderId = InetAddress.getLocalHost().getHostName();

			String headMessageToSendStr = _REPLY_HEAD + " " + version + " " + thisSenderId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF;
			byte[] messageToSend = M_Socket.joinMessageToChunk(headMessageToSendStr, data);

			mdr.send(messageToSend);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public byte[] restoreChunk(final String version, final String fileId, final int chunkNum){
		try{
			final String thisSenderId = InetAddress.getLocalHost().getHostName();

			String headMessageToSendStr = _HEAD + " " + version + " " + thisSenderId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF;
			byte[] messageToSend = headMessageToSendStr.getBytes();

			mc.send(messageToSend);

			ExecutorService executor = Executors.newSingleThreadExecutor();
			Callable<byte[]> callable = new Callable<byte[]>() {
				@Override
				public byte[] call() throws Exception {
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
						if(chunkSenderId.equals(thisSenderId)) continue;

						// Id of the chunk file received
						String chunkFileId = message[3];
						if(!chunkFileId.equals(fileId)) continue;

						// Num of the chunk file received
						int numOfChunkReceived = Integer.parseInt(message[4]);
						if(numOfChunkReceived != chunkNum) continue;

						byte[] chunkData = M_Socket.getChunkData(data);

						return chunkData;
					}
				}
			};

			Future<byte[]> future = executor.submit(callable);

			return future.get();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}

		return null;
	}

	public byte[] restoreFile(String fileName, String version){
		String fileId = null;
		for(Map.Entry<String, String> entry : fileIdToFileName.entrySet()){
			if(entry.getValue().equals(fileName)){
				fileId = entry.getKey();
				break;
			}
		}
		
		if(fileId == null) return null;
		
		ArrayList<byte[]> dataHolderTemp = new ArrayList<>();
		int chunkNum = 0;
		byte[] tempData = null;
		do{
			tempData = restoreChunk(version, fileId, chunkNum);
			if(tempData == null) return null;
			
			dataHolderTemp.add(tempData);
			chunkNum++;
		}while(tempData.length == FileManager._CHUNK_SIZE);
		
		byte[] restoredFile = new byte[0];
		for(byte[] restoredData : dataHolderTemp){
			byte[] addDataCopy = restoredFile;
			restoredFile = new byte[addDataCopy.length + restoredData.length];
			System.arraycopy(addDataCopy, 0, restoredFile, 0, addDataCopy.length);
			System.arraycopy(restoredData, 0, restoredFile, addDataCopy.length, restoredData.length);
		}
		
		return restoredFile;
	}
}
