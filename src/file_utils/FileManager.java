package file_utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class FileManager {

	private static String _POSTBOX = "PostBox";
	private static String _STORAGE = "_storage_folder_SYS_";
	
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
			fos.write(data);
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
}
