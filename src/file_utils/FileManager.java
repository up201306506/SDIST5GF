package file_utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
		File chunkFolder = new File(_STORAGE + "/" + fileId);
		if(!chunkFolder.exists()){
			chunkFolder.mkdir();
		}
		
		return (_STORAGE + "/" + fileId + "/");
	}

	public ArrayList<byte[]> splitFile(String filePath){
		
		ArrayList<byte[]> result = new ArrayList<>();
		
		File fileToSplit = new File(filePath);
		if(!fileToSplit.exists() || !fileToSplit.isFile()) return null;

		byte[] buffer = new byte[_CHUNK_SIZE];

		BufferedInputStream bis;

		try {

			bis = new BufferedInputStream(new FileInputStream(fileToSplit));

			long numberOfChuncks = fileToSplit.length() / _CHUNK_SIZE;
			for(int i = 0; i < numberOfChuncks; i++){
				bis.read(buffer);
				result.add(buffer);
			}
			
			int bytesRead = bis.read(buffer);
			byte[] smallBuffer = new byte[bytesRead];
			System.arraycopy(buffer, 0, smallBuffer, 0, bytesRead);
			result.add(smallBuffer);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	public boolean uniteFile(String fileName){
		String filesDir = _STORAGE;
		String outputDir = _POSTBOX + "/" + fileName;
		
		File[] files = new File(filesDir).listFiles();
		ArrayList<String> chunckNameHolder = new ArrayList<>();
		for(File file : files){
			String chunckName = file.getName();
			
			if(chunckName.length() >= fileName.length() &&
					chunckName.substring(0, fileName.length()).equals(fileName)){
				chunckNameHolder.add(chunckName);
			}
		}
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PriorityQueue<String> namesOrdered = new PriorityQueue<>(chunckNameHolder);
		while(!namesOrdered.isEmpty()){
			byte[] holderBuffer;
			try {
				holderBuffer = Files.readAllBytes(Paths.get(filesDir + "/" + namesOrdered.remove()));
				output.write(holderBuffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try{
			FileOutputStream outFile = new FileOutputStream(outputDir);
			outFile.write(output.toByteArray());
			outFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
