package Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import file_utils.FileManager;

public class Test_File_Breakdown {

	/*
		Teste de splitting e reconstrução de um ficheiro  em chunks
	*/
	public static void main(String[] args) {
			
		FileManager flmn = new FileManager();
		
		System.out.println("The file test.png in folder PostBox will now be broken down.");
		System.out.println("Press Enter to break the file into chunks.");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		
		
		String chunkFolderPath =  flmn.storeFolder("test.PNG");
		ArrayList<byte[]> data = flmn.splitFile("PostBox/test.PNG");
		
		for(int i = 0; i < data.size(); i++)
		{
			String chunkFileName = chunkFolderPath + "test.PNG" + "-" + i;
			File chunkFile = new File(chunkFileName);
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(chunkFile);
				fos.write(data.get(i));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//--Not working--
		System.out.println("The file should now have been split on the folder Test");
		System.out.println("Delete the Test.png file so we can regenerate it from chunks");
		System.out.println("Press Enter to attempt recoverig the file");
		scanner.nextLine();
		//flmn.uniteFile("test.PNG");
		
		
		scanner.close();
		return;
	}
}