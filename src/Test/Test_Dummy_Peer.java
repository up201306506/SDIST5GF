package Test;

import java.net.*;
import java.io.*;

/**
 * This peer is supposed to act like a real peer from the perspective of a TestApp Interface
 * However, it will not actually process any information, it will merely give confirmation responses after a delay.
 */
public class Test_Dummy_Peer {
	
	static int _PORT = 2346;
	
	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static BufferedReader clientReader = null;
	private static PrintWriter clientWriter = null;
	
	public static void main(String[] args) {
		
		boolean terminate = false;
		while(!terminate)
		{
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
				System.err.println("Error during Setup");
				e.printStackTrace();
				System.exit(-1);
			}          
			
			//-------------------------
			// Waiting for Handshake
			//-------------------------
			try 
			{
				String buffer;
				Boolean handshake = false;
				while(!handshake)
				{
					if(clientReader.ready())
					{
						buffer = clientReader.readLine();
						if(buffer.equals("Distributed Backup Service Interface"))
						{
							clientWriter.println("Distributed Backup Service Peer");
							handshake = true;
						}
						else //quit inner loop without confirming handshake
						{
							clientWriter.println("ERROR");
							break;
						}
					}
				}
				if(!handshake)
				{
					System.out.println("A client connected but it didn't handshake correctly, reseting...");
					bail();
					continue; //Resets main loop
				}	
			} catch (IOException e) 
			{
				System.err.println("Error during handshake");
				e.printStackTrace();
				System.exit(-1);
			}
			
			
			//-------------------------
			// Waiting FILE or RECLAIM command
			//-------------------------
						
			
			//-------------------------
			// Terminate
			//-------------------------
			
			bail();
			terminate = true;
		}
		
		
		
		
		
		System.out.println("Test_Dummy_Peer OK!");
		System.exit(0);
	}
	
	
	/**
	 * Closes the TCP resources, so the main loop may either reset or terminate
	 * 
	 */
	private static void bail(){
		try {
			serverSocket.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return;
	}
}
