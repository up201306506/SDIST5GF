package network_communications;

import java.io.IOException;

public class MC_Socket extends M_Socket{
	
	static private int BUFFER_LENGTH = 64000;
	
	public MC_Socket(String address, int p) throws IOException {
		super(address, p, BUFFER_LENGTH);
	}

	@Override
	public Boolean send() {
		// TODO Auto-generated method stub
		return null;
	}
}
