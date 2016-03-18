package network_communications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import file_utils.ProtocolEnum;

public class M_Socket {
	
	private static int _BUFFER_LENGTH = 64128;
	
	private InetAddress multicastAddress;
	private static byte[] bufferData;
	private int port;
	
	private MulticastSocket multicastSocket;
	private DatagramSocket datagramSocket;
	private DatagramPacket packet;
	
	private Map<Integer, Queue<String>> messageQueue;
	
	private Thread multicastReceiveThread;
	
	public M_Socket(String address, int p) {
		try{
			multicastAddress = InetAddress.getByName(address);
			bufferData = new byte[_BUFFER_LENGTH];
			port = p;
			
			messageQueue = new HashMap<>();			
			messageQueue.put(ProtocolEnum.UNKNOWN, new LinkedList<String>());
			for(int i = ProtocolEnum.min; i <= ProtocolEnum.max; i++)
				messageQueue.put(i, new LinkedList<String>());
			
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
						int blankSpaceIndex = tmp.indexOf(" ");
						if(blankSpaceIndex != -1)
						{
							switch (tmp.substring(0, blankSpaceIndex)) {
								case "PUTCHUNK":
									messageQueue.get(ProtocolEnum.BACKUP).add(tmp);
									break;
								case "STORED":
									messageQueue.get(ProtocolEnum.STORED).add(tmp);
									break;
								case "GETCHUNK":
									messageQueue.get(ProtocolEnum.GETCHUNK).add(tmp);
									break;
								case "CHUNK":
									messageQueue.get(ProtocolEnum.CHUNK).add(tmp);
									break;
								case "DELETE":
									messageQueue.get(ProtocolEnum.DELETE).add(tmp);
									break;
								case "REMOVED":
									messageQueue.get(ProtocolEnum.REMOVED).add(tmp);
									break;
								default:
									messageQueue.get(ProtocolEnum.UNKNOWN).add(tmp);
									break;
							}
						}
						else
							messageQueue.get(ProtocolEnum.UNKNOWN).add(tmp);
						
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
	
	public String receive(int protocolEnum) {
		String tmp = null;	
		if(protocolEnum >= ProtocolEnum.min && protocolEnum <= ProtocolEnum.max)
		{
			tmp = messageQueue.get(protocolEnum).peek();
			if(tmp != null) messageQueue.get(protocolEnum).remove();		
		}
		else
		{
			tmp = messageQueue.get(ProtocolEnum.UNKNOWN).peek();
			if(tmp != null) messageQueue.get(ProtocolEnum.UNKNOWN).remove();
		}

		return tmp;
	};
	
	public int  queueSize(int protocolEnum) {
		if (protocolEnum >= ProtocolEnum.min && protocolEnum <= ProtocolEnum.max) 
			return messageQueue.get(protocolEnum).size();
		else
			return messageQueue.get(ProtocolEnum.UNKNOWN).size();
		
	};
}