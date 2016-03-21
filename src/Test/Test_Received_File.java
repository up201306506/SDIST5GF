package Test;

import java.util.ArrayList;

import file_utils.FileManager;

public class Test_Received_File {

	public static void main(String[] args) {		
		FileManager fm = new FileManager();		
		
		/*ArrayList<byte[]> data = fm.splitFile("PostBox/music.mp3");
		for(int i = 0; i < data.size(); i++){
			fm.writeInStoreFolderFile("test", "" + i, data.get(i));
		}
		fm.uniteFile("test");*/
		
		String fileId = "59ac884729fabd71a288e0a5b68a4b8db1970ec12a62543fb41d365361aab8";
		fm.uniteFile(fileId);
	}
}
