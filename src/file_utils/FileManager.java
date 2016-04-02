package file_utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import peer_main.Peer;

public class FileManager {

	private String _thisPeerId;

	private static String _METADATA;
	private long freeDiskSpace;

	private static String _POSTBOX;
	private static String _STORAGE;
	private static String _FID_TO_NAME;
	private static String _STORECHUNK_REPLICATION;
	private static String _FILE_DATA_SEPARATOR = "---";

	public static int _CHUNK_SIZE = 64000;

	public FileManager(String peerId){
		_thisPeerId = peerId;

		_METADATA = _thisPeerId + File.separator + "metadata.txt";
		_POSTBOX = _thisPeerId + File.separator + "PostBox";
		_STORAGE = _thisPeerId + File.separator + "ChunkStorage";
		_FID_TO_NAME = _thisPeerId + File.separator + "FIdNames.txt";
		_STORECHUNK_REPLICATION = _thisPeerId + File.separator + "StoreChunkReplication.txt";
		
		File dirPeer = new File(_thisPeerId);
		if(!dirPeer.exists()){
			dirPeer.mkdir();
		}

		try{
			File metadataFile = new File(_METADATA);
			if(!metadataFile.exists()) {
				metadataFile.createNewFile();
				FileOutputStream metaOutFile = new FileOutputStream(metadataFile);
				metaOutFile.write(("" + Peer.maxDiskSpace).getBytes());
				metaOutFile.close();
				freeDiskSpace = Peer.maxDiskSpace;
			}else{
				FileInputStream metaInFile = new FileInputStream(metadataFile);
				byte[] data = new byte[(int) metadataFile.length()];
				metaInFile.read(data);
				freeDiskSpace = Long.parseLong(new String(data));
				metaInFile.close();
			}
		}catch (IOException e){
			e.printStackTrace();
		}

		File dirPostBox = new File(_POSTBOX);
		if(!dirPostBox.exists()){
			dirPostBox.mkdir();
		}

		File dirStorageFolder = new File(_STORAGE);
		if(!dirStorageFolder.exists()){
			dirStorageFolder.mkdir();
		}
	}

	public long getFreeDiskSpace(){
		return freeDiskSpace;
	}
	
	public boolean setFreeDiskSpace(long newDiskSpace){
		if(newDiskSpace <= Peer.maxDiskSpace)
			freeDiskSpace = newDiskSpace;
		else
			return false;
		
		try {
			PrintWriter writer = new PrintWriter(_METADATA);
			writer.print(freeDiskSpace);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	public String storeFolder(String fileId){
		File chunkFolder = new File(_STORAGE + File.separator + fileId);
		if(!chunkFolder.exists()){
			chunkFolder.mkdir();
		}

		return (_STORAGE + File.separator + fileId + File.separator);
	}

	public void writeInStoreFolderFile(String fileId, int chunkNum, byte[] data){
		String filePath = storeFolder(fileId) + fileId + "-" + String.format("%06d", chunkNum);
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			if(data != null) fos.write(data);
			else fos.write(("").getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}
	}

	public boolean uniteFile(String fileId, String fileName){
		String filesDir = _STORAGE + File.separator + fileId;
		String outputDir = _POSTBOX + File.separator + fileName;

		File[] files = new File(filesDir).listFiles();
		ArrayList<String> chunkNameHolder = new ArrayList<>();
		for(File file : files){
			String chunkName = file.getName();

			if(chunkName.length() >= fileId.length() &&
					chunkName.substring(0, fileId.length()).equals(fileId)){
				chunkNameHolder.add(chunkName);
			}
		}

		if(chunkNameHolder.size() == 0) return false;
		PriorityQueue<String> namesOrdered = new PriorityQueue<>(chunkNameHolder);

		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outputDir));
			while(!namesOrdered.isEmpty()){
				File tempFile = new File(filesDir + File.separator + namesOrdered.remove());
				Files.copy(tempFile.toPath(), bos);
			}

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}

		return true;
	}

	public ArrayList<byte[]> splitFile(String filePath){

		ArrayList<byte[]> result = new ArrayList<>();

		File fileToSplit = new File(filePath);
		if(!fileToSplit.exists() || !fileToSplit.isFile()) return null;

		try {

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSplit));

			byte[] buffer = new byte[_CHUNK_SIZE];
			long numberOfChuncks = fileToSplit.length() / _CHUNK_SIZE;
			for(int i = 0; i < numberOfChuncks; i++){
				bis.read(buffer);
				result.add(buffer);
				buffer = new byte[_CHUNK_SIZE];
			}

			int bytesRead = bis.read(buffer);
			if(bytesRead == -1) bytesRead = 0;
			byte[] smallBuffer = new byte[bytesRead];
			System.arraycopy(buffer, 0, smallBuffer, 0, bytesRead);
			result.add(smallBuffer);

			bis.close();

		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}

		return result;
	}

	public void deleteInstance(File instance){
		File[] files = instance.listFiles();

		if(files != null){
			for(File file : files){
				if(file == null) continue;
				
				if(file.isDirectory())
					deleteInstance(file);
				else
					file.delete();
			}
		}
		
		File parentFolder = new File(_STORAGE + File.separator + instance.getParentFile().getName());
		
		instance.delete();
		
		if(parentFolder != null && folderSize(parentFolder) == 0) parentFolder.delete();
	}

	private long folderSize(File folder){
		if(folder == null || !folder.isDirectory()) return 0;
		
		long length = 0;
		for(File file : folder.listFiles()){
			if(file == null) continue;
			
			if(file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}
	
	public long deleteFolder(String fileId){
		String filesDir = _STORAGE + File.separator + fileId;
		File chunkFolder = new File(filesDir);
		
		long dirSize = folderSize(chunkFolder);
		deleteInstance(chunkFolder);
		return dirSize;
	}

	// Read and Write chunk data to txt files
	public void writeFileIdToName(String fId, String fileName){
		try {
			PrintWriter pOut = new PrintWriter(new BufferedWriter(new FileWriter(_FID_TO_NAME, true)));
			pOut.println(_FILE_DATA_SEPARATOR);
			pOut.println(fId);
			pOut.println(fileName);
			pOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeFileIdToNameRegisters(Map<String, String> fileIdToName){
		try {
			PrintWriter writer = new PrintWriter(_FID_TO_NAME);
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		Iterator<Entry<String, String>> it = fileIdToName.entrySet().iterator();

		while(it.hasNext()){
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
			writeFileIdToName(pair.getKey(), pair.getValue());
		}
	}

	public Map<String, String> readFileIdToName(){
		try {
			new FileOutputStream(_FID_TO_NAME, true).close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<String, String> fIdToName = new ConcurrentHashMap<String, String>();
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new FileReader(_FID_TO_NAME));

			String line = bReader.readLine();
			while(line != null && !line.equals("")){
				if(!line.equals(_FILE_DATA_SEPARATOR)) return null;
				String fId = bReader.readLine();
				String fileName = bReader.readLine();

				if(!fIdToName.containsKey(fId))
					fIdToName.put(fId, fileName);

				line = bReader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally
		{
			try {
				bReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return fIdToName;
	}

	public void writeStoreChunkReplicationValue(StoreChunkKey storeChunk, ReplicationValue replicationValue){
		try {
			PrintWriter pOut = new PrintWriter(new BufferedWriter(new FileWriter(_STORECHUNK_REPLICATION, true)));
			pOut.println(_FILE_DATA_SEPARATOR);
			pOut.println(storeChunk.toString());
			pOut.println(replicationValue.toString());
			pOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void writeStoreChunkReplicationRegisters(Map<StoreChunkKey, ReplicationValue> storeChunkMap){
		try {
			PrintWriter writer = new PrintWriter(_STORECHUNK_REPLICATION);
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Map.Entry<StoreChunkKey, ReplicationValue> entry : storeChunkMap.entrySet())
			writeStoreChunkReplicationValue(entry.getKey(), entry.getValue());
	}

	public Map<StoreChunkKey, ReplicationValue> readStoreChunkReplicationRegisters(){
		try {
			new FileOutputStream(_STORECHUNK_REPLICATION, true).close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<StoreChunkKey, ReplicationValue> storeChunkReplicationRegisters = new ConcurrentHashMap<StoreChunkKey, ReplicationValue>();
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new FileReader(_STORECHUNK_REPLICATION));

			String line = bReader.readLine();
			while(line != null && !line.equals("")){
				if(!line.equals(_FILE_DATA_SEPARATOR)) return null;
				String fId = bReader.readLine();
				String chunkVersion = bReader.readLine();
				int chunkNum = Integer.parseInt(bReader.readLine());
				int chunkReplicationDegree = Integer.parseInt(bReader.readLine());
				int chunkReplicationValue = Integer.parseInt(bReader.readLine());

				if(!storeChunkReplicationRegisters.containsKey(new StoreChunkKey(fId, chunkVersion, chunkNum)))
					storeChunkReplicationRegisters.put(new StoreChunkKey(fId, chunkVersion, chunkNum), new ReplicationValue(chunkReplicationDegree, chunkReplicationValue));

				line = bReader.readLine();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally
		{
			try {
				bReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return storeChunkReplicationRegisters;
	}

	public byte[] readChunkData(String chunkVersion, String chunkFileId, int numOfChunk) {
		String chunkFilePath = _STORAGE + File.separator + chunkFileId + File.separator + chunkFileId + "-" + String.format("%06d", numOfChunk);

		try {
			File chunkFile = new File(chunkFilePath);
			FileInputStream fis = new FileInputStream(chunkFile);

			byte[] data = new byte[(int) chunkFile.length()];
			fis.read(data);

			fis.close();

			return data;

		} catch (IOException e) {
			e.printStackTrace();
			e.getCause();
		}

		return null;
	}
}