package interface_main;

public class TestApp {

	public static void main(String[] args) {
		
		//-------------------------
		// Check arguments
		//-------------------------
		if(args.length < 3 || args.length > 4)
		{
			System.out.println("Call Error: Wrong number of arguments");
			System.out.println("----------------------------");
			System.out.println("Usage: java TestApp <IP address>:<port number> <SubProt> <FilePath> <RepDeg>  ");
			System.out.println("<IP address>:<port number> - Address of the service Peer");
			System.out.println("<SubProt> - Sub protocol being tested, and must be one of: BACKUP, RESTORE, DELETE, RECLAIM.");
			System.out.println("<FilePath> - Path name of the file to backup/restore/delete");
			System.out.println("<RepDeg> - Replication Degree (BACKUP only)");
			
			System.exit(1);
		}
		
		//-------------------------
		// Handshake with Peer
		//-------------------------
		
		//-------------------------
		// Send Command to Peer
		//-------------------------
		
		//-------------------------
		// Wait Response from Peer
		//-------------------------
		
		//-------------------------
		// Terminate
		//-------------------------
		
		
		System.exit(0);
	}

}
