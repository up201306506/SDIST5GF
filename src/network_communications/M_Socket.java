package network_communications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import file_utils.ProtocolEnum;

public class M_Socket {
	
	private static int _BUFFER_LENGTH = 64512;
	
	private InetAddress multicastAddress;
	private static byte[] bufferData;
	private int port;
	
	private MulticastSocket multicastSocket;
	private DatagramSocket datagramSocket;
	private DatagramPacket packet;
	
	private Map<Integer, LinkedList<byte[]>> messageQueue;
	
	private Thread multicastReceiveThread;
	
	// Constructor
	public M_Socket(String address, int p) {
		try{
			multicastAddress = InetAddress.getByName(address);
			bufferData = new byte[_BUFFER_LENGTH];
			port = p;
			
			messageQueue = new ConcurrentHashMap<Integer, LinkedList<byte[]>>();
			messageQueue.put(ProtocolEnum.UNKNOWN, new LinkedList<byte[]>());
			for(int i = ProtocolEnum.min; i <= ProtocolEnum.max; i++)
				messageQueue.put(i, new LinkedList<byte[]>());
			
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
						packet = new DatagramPacket(bufferData, bufferData.length);
						multicastSocket.receive(packet);
						
						byte[] data = new byte[packet.getLength()];
						System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
						
						//byte[] data = packet.getData();
						String tmp = new String(data, 0, data.length);
						int blankSpaceIndex = tmp.indexOf(" ");
						if(blankSpaceIndex != -1)
						{
							switch (tmp.substring(0, blankSpaceIndex)) {
								case "PUTCHUNK":
									messageQueue.get(ProtocolEnum.BACKUP).add(data);
									break;
								case "STORED":
									messageQueue.get(ProtocolEnum.STORED).add(data);
									break;
								case "GETCHUNK":
									messageQueue.get(ProtocolEnum.GETCHUNK).add(data);
									break;
								case "CHUNK":
									messageQueue.get(ProtocolEnum.CHUNK).add(data);
									break;
								case "DELETE":
									messageQueue.get(ProtocolEnum.DELETE).add(data);
									break;
								case "REMOVED":
									messageQueue.get(ProtocolEnum.REMOVED).add(data);
									break;
								default:
									messageQueue.get(ProtocolEnum.UNKNOWN).add(data);
									break;
							}
						}
						else
							messageQueue.get(ProtocolEnum.UNKNOWN).add(data);
						
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
			}
		});
		multicastReceiveThread.start();
	}

	// Interaction functions
	public Boolean send(byte[] message) {
		
		try{
			DatagramPacket temppacket = new DatagramPacket(message, message.length, multicastAddress, port);		
			datagramSocket.send(temppacket);
		} catch (IOException e){
			e.printStackTrace();
		}
		
		return true;
	}
	
	public synchronized byte[] receive(int protocolEnum) {
		byte[] holder = null;
		if(messageQueue.get(protocolEnum).size() > 0){
			holder = messageQueue.get(protocolEnum).removeFirst();
		}
		return holder;
	}
	
	// Util functions
	public static byte[] joinMessageToChunk(String messageHeadStr, byte[] chunkData){
		byte[] messageHead = messageHeadStr.getBytes();
		byte[] messageToSend = new byte[messageHead.length + chunkData.length];
		
		System.arraycopy(messageHead, 0, messageToSend, 0, messageHead.length);
		System.arraycopy(chunkData, 0, messageToSend, messageHead.length, chunkData.length);
		
		return messageToSend;
	}
	
	public static String[] getMessage(byte[] data){
		if(data == null) return null;
		String[] tmpStringArray = (new String(data)).split("\\s+");
		
		String[] result;
		if(tmpStringArray[0].equals("PUTCHUNK")){
			result = new String[6];
		}else if(tmpStringArray[0].equals("STORED") || tmpStringArray[0].equals("GETCHUNK") ||
					tmpStringArray[0].equals("CHUNK") || tmpStringArray[0].equals("REMOVED") ){
			result = new String[5];
		}else if(tmpStringArray[0].equals("DELETE")){
			result = new String[4];
		}else{
			return null;
		}
		
		System.arraycopy(tmpStringArray, 0, result, 0, result.length);
		return result;
	}
	
	public static byte[] getChunkData(byte[] data){
		int bodyIndex = -1;
		for(int i = 0; i < data.length - 3; i++){
			if(data[i] == 0xD && data[i + 1] == 0xA &&
					data[i + 2] == 0xD && data[i + 3] == 0xA){
				bodyIndex = i + 4;
				break;
			}
		}
		
		if(bodyIndex > data.length) return null;
		
		byte[] result = new byte[data.length - bodyIndex];
		
		System.out.println(result.length);
		System.arraycopy(data, bodyIndex, result, 0, result.length);
		return result;
	}
}