package network_communications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.Queue;

public abstract class M_Socket {
	
	InetAddress multicastAddress;
	static byte[] bufferData;
	int port;
	
	Queue<String> messageQueue;
	
	MulticastSocket multicastSocket;
	DatagramSocket datagramSocket;
	
	DatagramPacket packet;
	
	Thread multicastReceiveThread;
	
	public M_Socket(String address, int p, int bufLength) throws IOException{
		
		multicastAddress = InetAddress.getByName(address);
		bufferData = new byte[bufLength];
		port = p;
		
		messageQueue = new LinkedList<String>();
		
		multicastSocket = new MulticastSocket(port);
		multicastSocket.joinGroup(multicastAddress);
		
		datagramSocket = new DatagramSocket();
		
		packet = new DatagramPacket(bufferData, bufferData.length);
		
		multicastReceiveThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						multicastSocket.receive(packet);
						String tmp = new String(packet.getData(), 0, packet.getLength());
						messageQueue.add(tmp);
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
			}
		});
		multicastReceiveThread.start();
	}
	
	public abstract Boolean send();
	
	public String receive() {
		String tmp = messageQueue.peek();
		
		if(tmp == null) tmp = "NADA";
		else messageQueue.remove();
		
		return tmp;
	};
}