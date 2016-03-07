package interface_main;

import java.io.IOException;

import network_communications.MC_Socket;

public class Proj {

	public static void main(String[] args) throws NumberFormatException, IOException, InterruptedException {
		MC_Socket p1 = new MC_Socket(args[0], Integer.parseInt(args[1]));
		
		while(true){
			System.out.println(p1.receive());
			Thread.sleep(1000);
		}
	}
}