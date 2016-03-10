package file_management;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Chunk {
	
	public File chunkFile;
	
	public Chunk(String pathToChunk) {
		chunkFile = new File(pathToChunk);
	}

	public String getData() {
		FileInputStream fis;
		byte[] data = null;
		
		try {
			fis = new FileInputStream(chunkFile);
			data = new byte[(int) chunkFile.length()];
			fis.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(data != null)
			return new String(data);
		else
			return null;
	}
}