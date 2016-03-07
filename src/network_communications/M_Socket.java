package network_communications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class M_Socket {
	
	InetAddress multicastAddress;
	byte[] bufferData;
	DatagramPacket packet;
	MulticastSocket socket;
	int port;
	
	public M_Socket(String address, int p) throws IOException{
		multicastAddress = InetAddress.getByName(address);
		port = p;
		socket = new MulticastSocket(port);
		socket.joinGroup(multicastAddress);
	}
	
	public abstract Boolean send();
	public abstract String receive();
	
	
}
