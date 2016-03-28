package Test;

import java.net.*;
import java.io.*;

/**
 * This peer is supposed to act like a real peer from the perspective of a TestApp Interface
 * However, it will not actually process any information, it will merely give confirmation responses after a delay.
 */
public class Test_Dummy_Peer {
	
	static int _PORT = 51111;
	
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
			boolean skipToReclaim = false;
			boolean fileexists = false;
			String[] fileArgs;
			
			try {
				String buffer;
				while(true)
				{
					if(clientReader.ready())
					{
						buffer = clientReader.readLine();
						fileArgs = buffer.split(" ");
						if(fileArgs[0].equals("RECLAIM")) //No FILE, skip to Subprotocol command
						{
							skipToReclaim = true;
							break;
						}

						/* LOGIC FOR CHECKING IF FILE EXISTS*/
							// fileArgs[1] - Filepath!
							fileexists = true; //dummy
						/* LOGIC FOR CHECKING IF FILE EXISTS*/
						
						if (fileexists)
							clientWriter.println("FILE OK");
						else
							clientWriter.println("FILE NOT FOUND");
						break;
					}
				}
				if(!fileexists && !skipToReclaim)
				{
					System.out.println("Filepath: " + fileArgs[1]);
					System.out.println("The client asked for a a file this peer doesn't recognize, reseting...");
					bail();
					continue; //Resets main loop
				}
			} catch (IOException e) {
				System.err.println("Error during FILE protocol");
				e.printStackTrace();
				System.exit(-1);
			}
			

			//-------------------------
			// Processing Subprotocol command
			//-------------------------
			if(!skipToReclaim)
			{
				String subProtocolType = null;
				int replicationDegree;
				
				try {
					String buffer;
					
					while(true)
					{
						if(clientReader.ready())
						{
							buffer = clientReader.readLine();
							if(!buffer.equals("RESTORE") &&  !buffer.equals("DELETE"))
							{
								String[] temp = buffer.split(" ");
								subProtocolType = temp[0];
								replicationDegree = Integer.parseInt(temp[1]);
							}
							else
								subProtocolType = buffer;
							break;
						}
					}
				} catch (IOException e) {
					System.err.println("Error during SUBPROTOCOL reception");
					e.printStackTrace();
					System.exit(-1);
				}

				System.out.println("Received a request to proccess a " + subProtocolType + " subprotocol");
				
				switch(subProtocolType)
				{
				case "BACKUP":

					//---------------------------
					// * BACKUP
					//---------------------------
					
					boolean backup_success = false;
					/* LOGIC FOR BACKUP*/
						//fileArgs[1] - filepath
						//replicationDegree
					backup_success = true; //dummy
					/* LOGIC FOR BACKUP*/
					
					if (backup_success)
						clientWriter.println("BACKUP OK");
					else
						clientWriter.println("BACKUP FAIL");
					
					break;
					
					
				case "RESTORE":
					//---------------------------
					// * RESTORE
					//---------------------------

					boolean restore_success = false;
					/* LOGIC FOR RESTORE*/
						//fileArgs[1] - filepath
					restore_success = true; //dummy
					/* LOGIC FOR RESTORE*/
					
					if (restore_success)
						clientWriter.println("RESTORE OK");
					else
						clientWriter.println("RESTORE FAIL");
					break;
					
				case "DELETE":

					//---------------------------
					// * DELETE
					//---------------------------
					
					boolean delete_success = false;
					/* LOGIC FOR DELETE*/
						//fileArgs[1] - filepath
					delete_success = true; //dummy
					/* LOGIC FOR DELETE*/
					
					if (delete_success)
						clientWriter.println("DELETE OK");
					else
						clientWriter.println("DELETE FAIL");
					
					break;
					
					
				default:
					System.out.println("The client asked for an unknown subprotocol request, restarting...");
					bail();
					continue; //Resets main loop
				}
				
				
			}
			else
			{
				System.out.println("Received a request to proccess a RECLAIM subprotocol");

				//---------------------------
				// * RECLAIM
				//---------------------------
				
				boolean reclaim_success = false;
				/* LOGIC FOR RECLAIMING*/
					// fileArgs[1] - Restore Amount.
					reclaim_success = true; //dummy
				/* LOGIC FOR RECLAIMING*/
				
				if (reclaim_success)
					clientWriter.println("RECLAIM OK");
				else
					clientWriter.println("RECLAIM FAIL");

			}

			//-------------------------
			// Terminate
			//-------------------------
			
			bail();
			if(true) //dummy
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
