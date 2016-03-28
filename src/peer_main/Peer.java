package peer_main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import file_utils.FileManager;
import file_utils.ReplicationValue;
import file_utils.StoreChunkKey;
import network_communications.M_Socket;

/**
 * Main class for launching a running Peer, and to which an Interface Application should be connected to.
 *
 */
public class Peer {
	
	public static M_Socket mc;
	public static M_Socket mdb; 
	public static M_Socket mdr; 
	
	public static String peerID;
	
	static FileManager fm;
	static Map<String, String> fileNames;
	static Map<StoreChunkKey, ReplicationValue> chunkStored;
	
	public static void main(String[] args) {
		
		//-------------------------
		// Checking Arguments
		//-------------------------
		if(args.length != 4)
		{
			/*
			 * Example Args: UniqueID 224.225.226.230:12345 224.225.226.231:12345 224.225.226.232:12345
			 */
			
			System.out.println("Call Error: Wrong number of arguments");
			System.out.println("----------------------------");
			System.out.println("Usage: java Peer <PeerID> <mcIP>:<mcPORT> <mdbIP>:<mdbPORT> <mdrIP>:<mdrPORT>");
			System.out.println("<PeerID> - name by which this ID is recognized, should be unique");
			System.out.println("<IP>:<PORT> - Multicast channel addresses for MC, MDB and MDR");
			System.exit(1);
		}
		
		//-------------------------
		// Initialising variables
		//-------------------------
		
		peerID = args[0];
		
		String[] MCArgs = args[1].split(":");
		String[] MDBArgs = args[2].split(":");
		String[] MDRArgs = args[3].split(":");
		mc = new M_Socket(MCArgs[0], Integer.parseInt(MCArgs[1]));
		mdb = new M_Socket(MDBArgs[0], Integer.parseInt(MDBArgs[1]));
		mdr = new M_Socket(MDRArgs[0], Integer.parseInt(MDRArgs[1]));
		
		
		fm = new FileManager();
		fileNames = fm.readFileIdToName();
		chunkStored = fm.readStoreChunkReplicationRegisters();
		
		
		peerLogic.start();
		
		
		//-------------------------
		// Instructions for stopping and Termination
		//-------------------------
		
		System.out.println("<Press any key to stop executing>");
		try {System.in.read();} 
			catch (IOException e) {e.printStackTrace();}
		
		
		fm.writeFileIdToNameRegisters(fileNames);
		fm.writeStoreChunkReplicationRegisters(chunkStored);
		
		System.out.println("Closing down.");
		System.exit(0);
	}
	
	
	
	
	static Thread peerLogic = new Thread(new Runnable() {
		
		public ServerSocket serverSocket;
		public Socket clientSocket;
		public BufferedReader clientReader = null;
		public PrintWriter clientWriter = null;
		
		
		public void run() {
			
			
			while(true)
			{
				//-------------------------
				// Initialising the TCP Ports
				//-------------------------
				try {
					serverSocket = new ServerSocket(0);
					System.out.println("Waiting for connection (Address: " + InetAddress.getLocalHost().getHostAddress() + " Port: " + serverSocket.getLocalPort() + ")... ... ...");
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
				System.out.println("The service with the client was completed.");
				bail();
			}
		}
		
		
		/**
		 * Closes the TCP resources, so the main loop may either reset or terminate
		 * 
		 */
		private void bail(){
			try {
				serverSocket.close();
				clientSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			return;
		}
		
		
		
		
	});
}
