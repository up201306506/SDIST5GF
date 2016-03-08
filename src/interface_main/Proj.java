package interface_main;

import file_management.FileManager;

public class Proj {

	public static void main(String[] args) {
		FileManager fm = new FileManager();
		//fm.splitFile("PR.JPG");
		fm.uniteFile("PR.JPG");
		
		/*MC_Socket p1 = new MC_Socket(args[0], Integer.parseInt(args[1]));
		
		while(true){
			System.out.println(p1.receive());
			Thread.sleep(500);
		}*/
	}
}