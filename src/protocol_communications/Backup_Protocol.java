package protocol_communications;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import file_utils.ChunkKey;
import file_utils.FileManager;
import file_utils.ProtocolEnum;
import file_utils.RandomDelay;
import network_communications.M_Socket;

public class Backup_Protocol extends Protocol {

	private static String _HEAD = "PUTCHUNK";
	private static String _REPLY_HEAD = "STORED";

	private static int _MAX_NUMBER_OF_RETRIES = 5;
	private static int _INITIAL_REPLY_WAIT_TIME = 1; // seconds

	public Backup_Protocol(FileManager fm, Map<ChunkKey, Integer> cs, M_Socket mc, M_Socket mdb) {
		super(fm, cs, mc);
		this.mdb = mdb;
	}

	// Sending Data
	private boolean sendPutChunck(String version, String senderId, String fileId, int chunkNum, final int replicationDegree, byte[] chunkData) {
		if(chunkData == null) return false;

		final HashSet<String> storedSenderIds = new HashSet<>();

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		boolean backupComplete = false;

		ExecutorService receiveExecutor = Executors.newFixedThreadPool(1);
		Runnable receiveRunnable = new Runnable() {
			@Override
			public void run() {

				byte[] data = null;
				do{
					data = mc.receive(ProtocolEnum.STORED);
				}while(data == null);
				
				String[] message = M_Socket.getMessage(data);
				if(message.length != 5 && message == null) return;
				
				// STORED
				if(!message[0].equals(_REPLY_HEAD)) return;
				
				// Version to store
				String versionToStore = message[1];
				
				// Id of the STORED sender
				String storedSenderId = message[2];
				
				// Id of the STORED chunk file
				String chunkFileId = message[3];
				
				// Num of the STORED chunk
				int numOfChunkStored = Integer.parseInt(message[4]);

				storedSenderIds.add(storedSenderId);
				return;
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
			
			if(storedSenderIds.size() == replicationDegree) backupComplete = true;
		}

		if(backupComplete) chunkStored.put(new ChunkKey(fileId, chunkNum), replicationDegree);

		return backupComplete;
	}

	public boolean backupFile(String filePath, String version, int replicationDegree){
		ArrayList<byte[]> data = fm.splitFile(filePath);
		if(data == null) return false;

		try {
			String senderId = InetAddress.getLocalHost().getHostName();

			File fileTemp = new File(filePath);
			String fileName = fileTemp.getName();
			String fileDateModified = "" + fileTemp.lastModified();

			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update((fileName + fileDateModified).getBytes());
			byte[] mdBytes = md.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < mdBytes.length; i++) {
				hexString.append(Integer.toHexString(0xFF & mdBytes[i]));
			}

			String fileId = hexString.toString();

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

		return true;
	}

	// Sending Receiving Confirmation
	private boolean sendStoredChunck(String version, String senderId, String fileId, int chunkNum) {

		try {
			Thread.sleep(RandomDelay.randomInt(0, 400));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		String messageToSend = _REPLY_HEAD + " " + version + " " + senderId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF;
		mc.send(messageToSend.getBytes());

		return true;
	}

	public boolean receiveChunk(){
		byte[] data = mdb.receive(ProtocolEnum.BACKUP);
		if(data == null) return false;
		
		String[] message = M_Socket.getMessage(data);
		if(message.length != 6 && message == null) return false;
		
		// PUTCHUNK
		if(!message[0].equals("PUTCHUNK")) return false;
		
		// Version of the chunk received
		String chunkVersionReceived = message[1];
		
		// Id of the PUTCHUNK sender
		String backupSenderId = message[2];
		
		// Id of the chunk file to store
		String chunkFileId = message[3];
		
		// Num of the chunk file to store
		int numOfChunkToStore = Integer.parseInt(message[4]);
		
		// Replication degree of the chunk to store
		int chunkReplicationDegree = Integer.parseInt(message[5]);
		
		byte[] chunkData = M_Socket.getChunkData(data);
		
		fm.writeInStoreFolderFile(chunkFileId, numOfChunkToStore, chunkData);

		String senderId;
		try {
			senderId = InetAddress.getLocalHost().getHostName();

			if(!sendStoredChunck(chunkVersionReceived, senderId, chunkFileId, numOfChunkToStore)) return false;
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
