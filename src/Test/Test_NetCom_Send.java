package Test;

import java.io.IOException;
import java.util.Scanner;

import network_communications.M_Socket;

public class Test_NetCom_Send {

	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
		
		/*
			Teste de emissão de mensagens:
				Pede e envia mensagens no canal de comunicação.
		*/
		
		String address = "224.225.226.230";
		String port = "12345";
		
		M_Socket p1 = new M_Socket(address, Integer.parseInt(port));
		
		
		Scanner reader = new Scanner(System.in);
		while(true)
		{
			System.out.println("Enter a message to send: ('q' to quit)");
			String m = reader.nextLine();
			
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