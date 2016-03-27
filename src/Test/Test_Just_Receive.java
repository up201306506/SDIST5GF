package Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Test_Just_Receive {

	public static void main(String[] args) throws InterruptedException, NumberFormatException, IOException {
		//Teste de recepção simples de mensagens por um multicastsocket

		if(args.length != 2){
			System.err.println("Usage: <Multicast Address> <Port>");
			return;
		}

		String address = args[0];
		int port = Integer.parseInt(args[1]);

		InetAddress multicastAddress = InetAddress.getByName(address);
		byte[] bufferData = new byte[64128];

		MulticastSocket multicastSocket = new MulticastSocket(port);
		multicastSocket.joinGroup(multicastAddress);
		DatagramSocket datagramSocket = new DatagramSocket();
		DatagramPacket packet = new DatagramPacket(bufferData, bufferData.length);
		
		while(true){
			
			multicastSocket.receive(packet);
			String tmp = new String(packet.getData(), 0, packet.getLength());
			
			if(tmp == null) System.out.println("Vazio");
			else System.out.println("MSG: " + tmp.substring(0, 10));
			
			Thread.sleep(1000);
		}
	}
}
