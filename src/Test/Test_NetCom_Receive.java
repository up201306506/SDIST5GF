package Test;

import java.io.IOException;

import network_communications.M_Socket;

public class Test_NetCom_Receive {

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
			//System.out.println(p1.receive());
			Thread.sleep(500); // 1 minuto
		}
		return;
	}
}