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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

public class FileManager {

	private static String _POSTBOX = "PostBox";
	private static String _STORAGE = "ChunkStorage";
	private static String _FID_TO_NAME = "FIdNames.txt";
	private static String _STORECHUNK_REPLICATION = "StoreChunkReplication.txt";
	private static String _FILE_DATA_SEPARATOR = "---";

	private static int _CHUNK_SIZE = 64000;

	public FileManager(){
		File dirPostBox = new File(_POSTBOX);
		if(!dirPostBox.exists()){
			dirPostBox.mkdir();
		}

		File dirStorageFolder = new File(_STORAGE);
		if(!dirStorageFolder.exists()){
			dirStorageFolder.mkdir();
		}
	}

	public String storeFolder(String fileId){
		File chunkFolder = new File(_STORAGE + File.separator + fileId);
		if(!chunkFolder.exists()){
			chunkFolder.mkdir();
		}

		return (_STORAGE + File.separator + fileId + File.separator);
	}

	public void writeInStoreFolderFile(String fileId, int chunkNum, byte[] data){
		String filePath = storeFolder(fileId) + fileId + "-" + String.format("%05d", chunkNum);
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

		Map<String, String> fIdToName = new HashMap<>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(_FID_TO_NAME));

			String line = bReader.readLine();
			while(line != null && !line.equals("")){
				if(!line.equals(_FILE_DATA_SEPARATOR)) return null;
				String fId = bReader.readLine();
				String fileName = bReader.readLine();

				if(!fIdToName.containsKey(fId))
					fIdToName.put(fId, fileName);

				line = bReader.readLine();
			}

			bReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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

		Map<StoreChunkKey, ReplicationValue> storeChunkReplicationRegisters = new HashMap<>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(_STORECHUNK_REPLICATION));

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

			bReader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return storeChunkReplicationRegisters;
	}
}