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
			
			System.out.println("Default Queue Size: " + p1.queueSize(-1));
			System.out.println("BACKUP Queue Size: " + p1.queueSize(1));
			System.out.println("STORED Queue Size: " + p1.queueSize(2));
			
			Thread.sleep(2500);
			
			Thread.sleep(500);
			
		}
		return;
	}
}