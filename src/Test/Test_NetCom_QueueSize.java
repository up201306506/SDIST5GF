package Test;

import java.io.IOException;

import file_utils.ProtocolEnum;
import network_communications.M_Socket;

public class Test_NetCom_QueueSize {

	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
		
		/*
			Teste de recepção de mensagens:
				Recebe e faz display de mensagens no canal de comunicação.
		*/
		
		String address = "224.225.226.228";
		String port = "12346";
		
		M_Socket p1 = new M_Socket(address, Integer.parseInt(port));
		
		int cycles = 0;
		while(cycles < 120){ 
			System.out.println("----------------------------------------");
			System.out.println("Default Queue Size: " + p1.queueSize(-1));
			System.out.println("PUTCHUNK Queue Size: " + p1.queueSize(1));
			System.out.println("STORED Queue Size: " + p1.queueSize(2));
			System.out.println("GETCHUNK Queue Size: " + p1.queueSize(3));
			System.out.println("CHUNK Queue Size: " + p1.queueSize(4));
			System.out.println("DELETE Queue Size: " + p1.queueSize(5));
			System.out.println("REMOVED Queue Size: " + p1.queueSize(6));
			
			Thread.sleep(2500);
			
			Thread.sleep(500);
			
		}
		return;
	}
}