package network_communications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedList;
import java.util.Queue;

public class M_Socket {
	
	private static int _BUFFER_LENGTH = 64128;
	
	private InetAddress multicastAddress;
	private static byte[] bufferData;
	private int port;
	
	private MulticastSocket multicastSocket;
	private DatagramSocket datagramSocket;
	private DatagramPacket packet;
	
	private Queue<String> messageQueue;
	
	private Thread multicastReceiveThread;
	
	//Methods
	public M_Socket(String address, int p) {
		try{
			multicastAddress = InetAddress.getByName(address);
			bufferData = new byte[_BUFFER_LENGTH];
			port = p;
			messageQueue = new LinkedList<String>();
			multicastSocket = new MulticastSocket(port);
			multicastSocket.joinGroup(multicastAddress);
			datagramSocket = new DatagramSocket();
			packet = new DatagramPacket(bufferData, bufferData.length);
		}catch(IOException e){
			e.printStackTrace();
		}
		
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
	
	public Boolean send(String message) {
		
		try{
			DatagramPacket temppacket = new DatagramPacket(message.getBytes(), message.getBytes().length,
					multicastAddress, port);		
			datagramSocket.send(temppacket);
		} catch (IOException e){
			e.printStackTrace();
		}
		
		return true;
	}
	
	public String receive() {
		
		String tmp = messageQueue.peek();
		
		if(tmp != null) messageQueue.remove();
		
		return tmp;
	};
}