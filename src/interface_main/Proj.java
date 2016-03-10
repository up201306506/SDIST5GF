package interface_main;

import file_management.FileManager;
import network_communications.M_Socket;
import protocol_communications.Backup_Protocol;

public class Proj {

	public static void main(String[] args) {
		//FileManager fm = new FileManager();
		//fm.splitFile("test.JPG");
		//fm.uniteFile("PR.JPG");
		
		M_Socket mc = new M_Socket("224.225.226.227", 12345);
		M_Socket mdb = new M_Socket("224.225.226.228", 12346);
		
		Backup_Protocol bp = new Backup_Protocol(mc, mdb);
		
	}
}