package Test;

import java.io.IOException;

import file_utils.ProtocolEnum;
import network_communications.M_Socket;

public class Test_NetCom_Receive {

	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
		
		/*
			Teste de recepção de mensagens:
				Recebe e faz display de mensagens no canal de comunicação.
		*/
		
		String address = "224.225.226.230";
		String port = "12345";
		
		M_Socket p1 = new M_Socket(address, Integer.parseInt(port));
		
		int cycles = 0;
		while(cycles < 120){ 
						
			
			Boolean receivedFlag = false;
			
			
			String tmp = p1.receive(-1);
			if (tmp != null)
			{
				System.out.println("Got a message on Default Queue: " + tmp);
				receivedFlag = true;
			}
			for (int i = ProtocolEnum.min; i <= ProtocolEnum.max; i++)
			{
				tmp = p1.receive(i);
				if (tmp != null)
				{
					System.out.println("Got a message on Queue " + i + " : " + tmp);
					receivedFlag = true;
				}
			}		
			if(!receivedFlag)
				System.out.println("Nada");
			
			Thread.sleep(500); //2 minutos
			
		}
		return;
	}
}