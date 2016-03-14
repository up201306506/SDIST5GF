package protocol_communications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	private boolean sendPutChunck(String version, String senderId, String fileId, int chunkNum, final int replicationDegree, String chunkData) {
		if(chunkData == null) return false;

		int numOfTries = 1;
		int waitInterval = _INITIAL_REPLY_WAIT_TIME;
		
		boolean backupComplete = false;
		
		final HashSet<String> storedSenderIds = new HashSet<>();

		while(numOfTries <= _MAX_NUMBER_OF_RETRIES && !backupComplete){
			
			mdb.send(_HEAD + " " + version + " " + senderId + " " + fileId + " " + chunkNum + " " + replicationDegree +
					" " + _CRLF + _CRLF + chunkData);

			ExecutorService executor = Executors.newSingleThreadExecutor();
			try {
				executor.submit(new Runnable() {
					
					@Override
					public void run() {
						
						while(true){
							String data = mc.receive(ProtocolEnum.STORED);
							if(data != null){
								
								System.out.println("stored");
								
								// STORED Part
								int blankSpaceIndex = data.indexOf(" ");
								String holder = data.substring(0, blankSpaceIndex);
								//if(!holder.equals(_REPLY_HEAD)) return;

								// Version Part
								data = data.substring(blankSpaceIndex + 1);
								blankSpaceIndex = data.indexOf(" ");
								String versionStored = data.substring(0, blankSpaceIndex);
								//if(!versionStored.equals(versionSent)) return;

								// SenderId Part
								data = data.substring(blankSpaceIndex + 1);
								blankSpaceIndex = data.indexOf(" ");
								String storedPeerId = data.substring(0, blankSpaceIndex);
								
								// FileId Part
								data = data.substring(blankSpaceIndex + 1);
								blankSpaceIndex = data.indexOf(" ");
								String fileIdStored = data.substring(0, blankSpaceIndex);
								//if(!fileIdStored.equals(fileIdSent)) return;
								
								// ChunkNum Part
								data = data.substring(blankSpaceIndex + 1);
								blankSpaceIndex = data.indexOf(" ");
								int chunkNumStored = Integer.parseInt(data.substring(0, blankSpaceIndex));
								//if(chunkNumStored != chunkNumSent) return;
								
								// CRLF Part
								data = data.substring(blankSpaceIndex + 1);
								holder = data.substring(0, 2);
								
								storedSenderIds.add(storedPeerId);
								System.out.println(storedSenderIds.size());
								if(storedSenderIds.size() == replicationDegree) return;
							}
						}
					}
				}).get(waitInterval, TimeUnit.SECONDS);

			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				System.err.println("Receiving Socket may not have the receiving queue initialized for this protocol");
				e.printStackTrace();
			} catch (TimeoutException e) {
				System.out.println("TIMEOUT");
				
				numOfTries++;
				waitInterval = waitInterval * 2;
			}
			executor.shutdown();
			
			if(storedSenderIds.size() == replicationDegree) backupComplete = true;
		}

		chunkStored.put(new ChunkKey(fileId, chunkNum), replicationDegree);

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
				if(!sendPutChunck(version, senderId, fileId, i, replicationDegree, new String(data.get(i)))) return false;
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		System.out.println("backed up file");

		return true;
	}

	// Sending Receiving Confirmation
	private boolean sendStoredChunck(String version, String senderId, String fileId, String chunkNum) {

		try {
			Thread.sleep(RandomDelay.randomInt(0, 400));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		mc.send(_REPLY_HEAD + " " + version + " " + senderId + " " + fileId + " " + chunkNum + " " + _CRLF + _CRLF);

		return true;
	}

	public boolean receiveChunk(){
		String data = mdb.receive(ProtocolEnum.BACKUP);

		if(data == null) return false;

		int blankSpaceIndex;
		String holder;

		// PUTCHUNK Part
		blankSpaceIndex = data.indexOf(" ");
		holder = data.substring(0, blankSpaceIndex);
		if(!holder.equals(_HEAD)) return false;

		// Version Part
		data = data.substring(blankSpaceIndex + 1);
		blankSpaceIndex = data.indexOf(" ");
		String version = data.substring(0, blankSpaceIndex);

		//System.out.println("Version: " + version);

		// SenderId Part
		data = data.substring(blankSpaceIndex + 1);
		blankSpaceIndex = data.indexOf(" ");
		
		//String senderId = data.substring(0, blankSpaceIndex);
		//System.out.println("SenderId: " + senderId);

		// FileId Part
		data = data.substring(blankSpaceIndex + 1);
		blankSpaceIndex = data.indexOf(" ");
		String fileId = data.substring(0, blankSpaceIndex);

		String fileChunkFolderPath = fm.storeFolder(fileId);

		//System.out.println("FileId: " + fileId);

		// ChunkNum Part
		data = data.substring(blankSpaceIndex + 1);
		blankSpaceIndex = data.indexOf(" ");
		String chunkNum = data.substring(0, blankSpaceIndex);

		System.out.println("Chunk Num: " + chunkNum);

		// Replication Degree Part
		data = data.substring(blankSpaceIndex + 1);
		blankSpaceIndex = data.indexOf(" ");
		
		//String replicationDegree = data.substring(0, blankSpaceIndex);
		//System.out.println("Replication Degree: " + replicationDegree);

		// CRLF Part
		data = data.substring(blankSpaceIndex + 1);
		holder = data.substring(0, 2);

		// Chunk Data Part
		data = data.substring(2);
		byte[] chunkData = data.getBytes();

		String chunkFileName = fileChunkFolderPath + fileId + "-" + chunkNum;
		File chunkFile = new File(chunkFileName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(chunkFile);
			fos.write(chunkData);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String senderId;
		try {
			senderId = InetAddress.getLocalHost().getHostName();
			
			if(!sendStoredChunck(version, senderId, fileId, chunkNum)) return false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		return true;
	}
}
