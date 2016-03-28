package interface_main;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.*;

/**
 * TestApp as described in https://web.fe.up.pt/~pfs/aulas/sd2016/projs/proj1/proj1_svc_interface.html
 *
 */
public class TestApp {

	public static void main(String[] args) {
		
		ArrayList<String> subProtocolsFiles = new ArrayList<String>(Arrays.asList("BACKUP", "RESTORE", "DELETE"));
		boolean requiresFile = true;
		
		String peerAddress = null;
		int peerPort = 0;
		
		Socket echoSocket = null;
		PrintWriter peerWrite = null;
		BufferedReader peerRead = null;
		
		//-------------------------
		// Check arguments
		//-------------------------
		if(args.length < 3 || args.length > 4)
		{
			System.out.println("Call Error: Wrong number of arguments");
			System.out.println("----------------------------");
			System.out.println("Usage: java TestApp <IP address>:<port number> <SubProt> <FilePath> <RepDeg>  ");
			System.out.println("<IP address>:<port> - Address of the service Peer. You may use only <port> if the peer is in the local machine");
			System.out.println("<SubProt> - Sub protocol being tested, and must be one of: BACKUP, RESTORE, DELETE, RECLAIM.");
			System.out.println("<FilePath> - Path name of the file to backup/restore/delete");
			System.out.println("<RepDeg> - Replication Degree (BACKUP only)");
			
			System.exit(1);
		}
		
		
		if(!subProtocolsFiles.contains(args[1]))
		{
			if(args[1].equals("RECLAIM"))
				requiresFile = false;
			else
			{
				System.err.println("<SubProt> must be one of: BACKUP, RESTORE, DELETE, RECLAIM.");
				System.err.println("Instead was: " + args[1]);
				System.exit(1);
			}
		}
		
		String[] peerAddressArgs = args[0].split(":");
		if (peerAddressArgs.length == 2)
		{
			peerAddress = peerAddressArgs[0];
			peerPort = Integer.parseInt(peerAddressArgs[1]);
		}
		else
		{
			try {peerAddress = InetAddress.getLocalHost().getHostAddress();} 
			catch (UnknownHostException e) 
			{
				System.err.println("Problem resolving localhost, please use <IP address>:<port number> as the first argument");
				e.printStackTrace(); System.exit(-1);
			}
			peerPort = Integer.parseInt(args[0]);
		}
		System.out.println("Attempting connection to socket on address: " + peerAddress + ":" + peerPort);

		
		//-------------------------
		// Initialising the TCP Ports
		//-------------------------
		
		int connectionRetries = 10;
		while (connectionRetries > 0)
		{
			try 
			{
				echoSocket = new Socket(peerAddress, peerPort);
				peerWrite = new PrintWriter(echoSocket.getOutputStream(), true);
				peerRead = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
				connectionRetries = 0;
			} 
			catch (IOException e) 
			{
				System.err.println("Couldn't connect to " + peerAddress + ":" + peerPort);
				connectionRetries--;
				if(connectionRetries==0)
				{e.printStackTrace();System.exit(-1);}
			}
		}		
		System.out.println("Peer found! Sending handshake.");
		

		//-------------------------
		// Handshake with Peer
		//-------------------------
		
		if (!sendMessageWaitResponse("Distributed Backup Service Interface", "Distributed Backup Service Peer", peerRead, peerWrite))
		{
			System.err.println("Peer did not respond correctly to handshake, closing connection");
			try {echoSocket.close();} 
				catch (IOException e) {e.printStackTrace();}
			System.exit(-1);
		}
		System.out.println("Handshake Successful.");
		
		
		//-------------------------
		// Check if File Exists (BACKUP/RESTORE/DELETE)
		// Wait for response
		//-------------------------
		if(requiresFile)
		{		
			if (!sendMessageWaitResponse("FILE " + args[2], "FILE OK", peerRead, peerWrite))
			{
				System.err.println("Peer does not have the file you were looking for");
				try {echoSocket.close();} 
					catch (IOException e) {e.printStackTrace();}
				System.exit(-1);
			}
			System.out.println("Target file was found.");
		}
		
		//-------------------------
		// Send Command to Peer
		// Wait for response
		//-------------------------

		String message = args[1];
		if(args[1].equals("BACKUP"))
			message.concat(" " + args[3]);
		if(args[1].equals("RECLAIM"))
			message.concat(" " + args[2]);
		String response = args[1] + " OK";
		if (!sendMessageWaitResponse(message, response, peerRead, peerWrite))
		{
			System.out.println("The " + args[1] + " command was unsuccesful on the peer's end");
			try {echoSocket.close();} 
				catch (IOException e) {e.printStackTrace();}
			System.exit(-1);
		}
		
		//-------------------------
		// Terminate
		//-------------------------
		
		try {echoSocket.close();} 
			catch (IOException e) {e.printStackTrace();}
		System.out.println("TestApp OK!");
		System.exit(0);
	}
	
	
	/**
	 * Sends a message through a socket's buffers and awaits a response
	 * 
	 * @param message - Message to be sent through the Socket
	 * @param response - Response expected
	 * @param peerRead
	 * @param peerWrite
	 * @return = true upon success, false upon failure
	 */
	public static boolean sendMessageWaitResponse(String message, String response, BufferedReader peerRead, PrintWriter peerWrite){
		
		String buff = null;
		peerWrite.println(message);
		try {
			while(!peerRead.ready())
			{}
			buff = peerRead.readLine();
			if(!buff.equals(response))
			{
				System.err.println("Unexpected Response from Peer: " + buff);
				return false;
			}
		} catch (IOException e) {e.printStackTrace(); System.exit(-1);}
		return true;
	}
}
