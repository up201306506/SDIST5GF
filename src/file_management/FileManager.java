package file_management;

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

	public boolean splitFile(String fileName){
		String pathToFile = _POSTBOX + "/" + fileName;
		String outputDir = _STORAGE + "/";

		File fileToSplit = new File(pathToFile);
		if(!fileToSplit.exists() || !fileToSplit.isFile()) return false;

		byte[] buffer = new byte[_CHUNK_SIZE];

		BufferedInputStream bis;

		try {

			bis = new BufferedInputStream(new FileInputStream(fileToSplit));

			long numberOfChuncks = fileToSplit.length() / _CHUNK_SIZE;
			for(int i = 0; i < numberOfChuncks; i++){

				int bytesRead = bis.read(buffer);

				File chunck = new File(outputDir + fileName + "-" + (i + 1));
				FileOutputStream outputFile = new FileOutputStream(chunck);
				outputFile.write(buffer, 0, bytesRead);
				outputFile.close();
			}
			
			int bytesRead = bis.read(buffer);

			File chunck = new File(outputDir + fileName + "-" + (numberOfChuncks + 1));
			FileOutputStream outputFile = new FileOutputStream(chunck);
			outputFile.write(buffer, 0, bytesRead);
			outputFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
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
