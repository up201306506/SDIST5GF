package Test;

import java.net.*;
import java.io.*;

/**
 * This peer is supposed to act like a real peer from the perspective of a TestApp Interface
 * However, it will not actually process any information, it will merely give confirmation responses after a delay.
 */
public class Test_Dummy_Peer {
	
	static int _PORT = 2346;
	private Thread responseThread;
	
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static BufferedReader clientReader = null;
	private static PrintWriter clientWriter = null;
	
	public static void main(String[] args) {
		
		//-------------------------
		// Initialising the TCP Ports
		//-------------------------

		try {
			serverSocket = new ServerSocket(_PORT);
			System.out.println("Waiting for connection (Address: " + InetAddress.getLocalHost().getHostAddress() + " Port: " + _PORT + ")... ... ...");
			clientSocket = serverSocket.accept();
			System.out.println("A connection was made to " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
			clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (NumberFormatException | IOException e) {
			System.out.println("Error during Setup");
			e.printStackTrace();
			System.exit(-1);
		}           
		
		//-------------------------
		// Waiting for Handshake
		//-------------------------
		
		String message;
		Boolean handshake = false;
		try 
		{
			while(!handshake)
			{
				if(clientReader.ready())
				{
					message = clientReader.readLine();
					if(message.equals("Distributed Backup Service Interface"))
					{
						clientWriter.println("Distributed Backup Service Peer");
						handshake = true;
					}
				}
			}
			
		} catch (IOException e) {e.printStackTrace();System.exit(-1);}
		
		
		//-------------------------
		// Waiting for Handshake
		//-------------------------
		
		System.out.println("Test_Dummy_Peer OK!");
		System.exit(0);
	}

}
