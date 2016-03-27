package protocol_communications;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import file_utils.StoreChunkKey;
import file_utils.FileManager;
import file_utils.ProtocolEnum;
import file_utils.RandomDelay;
import file_utils.ReplicationValue;
import network_communications.M_Socket;

public class Backup_Protocol extends Protocol {

	private static String _HEAD = "PUTCHUNK";
	private static String _REPLY_HEAD = "STORED";

	private static int _MAX_NUMBER_OF_RETRIES = 5;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds

	private Thread receiveChunkThread;
	private Thread receiveStoredThread;

	public Backup_Protocol(final FileManager fm, Map<String, String> fIfN, Map<StoreChunkKey, ReplicationValue> cs, final M_Socket mc, final M_Socket mdb) {
		super(fm, fIfN, cs, mc);
		this.mdb = mdb;

		receiveChunkThread = new Thread(new Runnable() {
			public void run() {	
				while(true){
					byte[] data = null;
					do{
						data = mdb.receive(ProtocolEnum.BACKUP);
					}while(data == null);

					String[] message = M_Socket.getMessage(data);
					if(message == null || message.length != 6) continue;

					// PUTCHUNK
					if(!message[0].equals("PUTCHUNK")) continue;

					// Version of the chunk received
					String chunkVersionReceived = message[1];

					// Id of the PUTCHUNK sender
					String backupSenderId = message[2];
					String thisSenderId = null;
					try{
						thisSenderId = InetAddress.getLocalHost().getHostName();
						if(backupSenderId.equals(thisSenderId)) continue;

					} catch (UnknownHostException e) {
						e.printStackTrace();
					}

					// Id of the chunk file to store
					String chunkFileId = message[3];

					// Num of the chunk file to store
					int numOfChunkToStore = Integer.parseInt(message[4]);

					// Verifies if it is a chunk already received
					if(chunksStored.containsKey(new StoreChunkKey(chunkFileId, chunkVersionReceived, numOfChunkToStore))){
						boolean jumpToNext = false;

						for(Map.Entry<StoreChunkKey, ReplicationValue> entry : chunksStored.entrySet()){
							if(entry.getKey().equals(new StoreChunkKey(chunkFileId, chunkVersionReceived, numOfChunkToStore))){
								String version = entry.getKey().getVersion();
								if(Float.parseFloat(version) < Float.parseFloat(chunkVersionReceived))
									entry.getKey().setVersion(chunkVersionReceived);
								else
									jumpToNext = true;
								break;
							}
						}

						if(jumpToNext){
							jumpToNext = false;
							continue;
						}
					}

					// Replication degree of the chunk to store
					int chunkReplicationDegree = Integer.parseInt(message[5]);

					// Register the new received chunk
					chunksStored.put(new StoreChunkKey(chunkFileId, chunkVersionReceived, numOfChunkToStore), new ReplicationValue(chunkReplicationDegree, 1));

					byte[] chunkData = M_Socket.getChunkData(data);

					fm.writeInStoreFolderFile(chunkFileId, numOfChunkToStore, chunkData);

					try {
						Thread.sleep(RandomDelay.randomInt(0, 400));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					String messageToSend = _REPLY_HEAD + " " + chunkVersionReceived + " " + thisSenderId + " " +
							chunkFileId + " " + numOfChunkToStore + " " + _CRLF + _CRLF;
					mc.send(messageToSend.getBytes());
				}
			}
		});

		receiveStoredThread = new Thread(new Runnable() {
			public void run() {
				while(true){
					byte[] data = null;
					do{
						data = mc.receive(ProtocolEnum.STORED);
					}while(data == null);

					String[] message = M_Socket.getMessage(data);
					if(message == null || message.length != 5) continue;

					// STORED
					if(!message[0].equals(_REPLY_HEAD)) continue;

					// Version to store
					String versionStored = message[1];

					// Id of the STORED sender
					String storedSenderId = message[2];
					try{
						String thisSenderId = InetAddress.getLocalHost().getHostName();
						if(storedSenderId.equals(thisSenderId)) continue;

					} catch (UnknownHostException e) {
						e.printStackTrace();
					}

					// Id of the STORED chunk file
					String chunkStoredFileId = message[3];

					// Num of the STORED chunk
					int numOfChunkStored = Integer.parseInt(message[4]);

					if(chunksStored.containsKey(new StoreChunkKey(chunkStoredFileId, versionStored, numOfChunkStored))){
						chunksStored.get(new StoreChunkKey(chunkStoredFileId, versionStored, numOfChunkStored)).incrementReplicationValue();
						FileManager.writeStoreChunkReplicationRegisters(chunksStored);
					}
				}
			}
		});

		receiveChunkThread.start();
		receiveStoredThread.start();
	}

	// Sending Data
	private boolean sendPutChunck(final String version, String senderId, final String fileId, final int chunkNum, int replicationDegree, byte[] chunkData) {
		if(chunkData == null) return false;

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		boolean backupComplete = false;

		if(!chunksStored.containsKey(new StoreChunkKey(fileId, version, chunkNum)))
			chunksStored.put(new StoreChunkKey(fileId, version, chunkNum), new ReplicationValue(replicationDegree, 0));

		ExecutorService receiveExecutor = Executors.newFixedThreadPool(1);
		Runnable receiveRunnable = new Runnable() {
			@Override
			public void run() {
				while(!chunksStored.get(new StoreChunkKey(fileId, version, chunkNum)).replicationValueAboveOrEqualToDegree()){

				}
			}
		};

		String headMessageToSendStr = _HEAD + " " + version + " " + senderId + " " + fileId + " " + chunkNum + " " +
				replicationDegree + " " + _CRLF + _CRLF;

		byte[] messageToSend = M_Socket.joinMessageToChunk(headMessageToSendStr, chunkData);

		while(( numOfTries <= _MAX_NUMBER_OF_RETRIES ) && !backupComplete){

			mdb.send(messageToSend);

			try {
				receiveExecutor.submit(receiveRunnable).get(waitInterval, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
				e.getCause();
			} catch (TimeoutException e) {
				System.out.println("TIMEOUT");

				numOfTries++;
				waitInterval = waitInterval * 2;
			}

			if(chunksStored.containsKey(new StoreChunkKey(fileId, version, chunkNum)))
				if(chunksStored.get(new StoreChunkKey(fileId, version, chunkNum)).replicationValueAboveOrEqualToDegree())
					backupComplete = true;
		}

		receiveExecutor.shutdown();
		return backupComplete;
	}

	public boolean backupFile(String filePath, String version, int replicationDegree){
		ArrayList<byte[]> data = fm.splitFile(filePath);
		if(data == null) return false;

		String fileId = null;
		String fileName = null;
		try {
			String senderId = InetAddress.getLocalHost().getHostName();

			File fileTemp = new File(filePath);
			fileName = fileTemp.getName();

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(fileName.getBytes());
			byte[] mdBytes = md.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdBytes.length; i++) {
				hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
			}

			fileId = hexString.toString();

			for(int i = 0; i < data.size(); i++){
				System.out.println("Chunk: " + i + "\tSize: " + data.get(i).length);
				if(!sendPutChunck(version, senderId, fileId, i, replicationDegree, data.get(i))) return false;
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		System.out.println("Backed up file");

		if(fileId != null && fileName != null)
			if(!fileIdToFileName.containsKey(fileId)){
				fileIdToFileName.put(fileId, fileName);
				FileManager.writeFileIdToName(fileId, fileName);
			}

		return true;
	}
}
