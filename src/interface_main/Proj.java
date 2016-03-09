package interface_main;

import file_management.FileManager;

public class Proj {

	public static void main(String[] args) {
		FileManager fm = new FileManager();
		//fm.splitFile("PR.JPG");
		fm.uniteFile("PR.JPG");
		
	}
}