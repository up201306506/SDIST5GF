package Test;

import java.io.IOException;

import file_utils.ProtocolEnum;
import network_communications.M_Socket;

public class Test_NetCom_Receive {

	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
		
		/*
			Teste de recep��o de mensagens:
				Recebe e faz display de mensagens no canal de comunica��o.
		*/
		
		String address = "224.225.226.228";
		String port = "12346";
		
		M_Socket p1 = new M_Socket(address, Integer.parseInt(port));
		
		int cycles = 0;
		while(cycles < 120){ 
						
			/*
			Boolean receivedFlag = false;
			
			if (p1.queueSize(-1) > 0)
			{
				System.out.println("Got a message on Default Queue: " + p1.receive(-1));
				receivedFlag = true;
			}
			for (int i = ProtocolEnum.min; i <= ProtocolEnum.max; i++)
			{
				if (p1.queueSize(i) > 0)
				{
					System.out.println("Got a message on " + i + " Queue: " + p1.receive(i));
					receivedFlag = true;
				}
			}		
			if(receivedFlag)
				System.out.println("Nada");
			*/
			
			Thread.sleep(500);
			
		}
		return;
	}
}