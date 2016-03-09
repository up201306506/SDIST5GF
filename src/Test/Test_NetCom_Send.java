package Test;

import java.io.IOException;
import java.util.Scanner;

import file_management.FileManager;
import network_communications.MC_Socket;

public class Test_NetCom_Send {

	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
		
		/*
			Teste de emissão de mensagens:
				Pede e envia mensagens no canal de comunicação.
		*/
		
		String address = "224.225.226.227";
		String port = "12345";
		
		MC_Socket p1 = new MC_Socket(address, Integer.parseInt(port));
		
		
		Scanner reader = new Scanner(System.in);
		while(true)
		{
			System.out.println("Enter a message to send: ('q' to quit)");
			String m = reader.next();
			
			if(m.equals("q"))
			{
				System.out.println("Done");
				return;
			}
			else
			{
				System.out.println('"'+m+"\" was sent through channel " + address + ':' + port);
				p1.send(m);
			}
				
		}
	
	}
}