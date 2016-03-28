package Test;

import java.io.IOException;
import java.util.Observer;
import java.util.Observable;

import file_utils.RandomDelay;



public class Test_Events {
	


	static Thread fakeGETCHUNK = new Thread(new Runnable() {
		int delay;
		boolean notblocked;
		
		public void run() {
			while(true)
			{
				notblocked = true;
				delay = RandomDelay.randomInt(1000, 3000);
				System.out.println("1: I GOT A GETCHUNK, I WILL SEND A CHUNK IN " + delay + "MILISECONDS");
				
				try {Thread.sleep(delay);} 
					catch (InterruptedException e) {e.printStackTrace();}
				
				if(notblocked)
					System.out.println("   1: CHUNK IS SENT!");
				else
					System.out.println("   1: XXXXXXXXXXXXXXXXXXXXXXXX");
			}
		}
	});
	static Thread fakeCHUNK = new Thread(new Runnable() {
		int delay;
		
		public void run() {
			while(true)
			{
				delay = RandomDelay.randomInt(1000, 5000);
				
				try {Thread.sleep(delay);} 
					catch (InterruptedException e) {e.printStackTrace();}
				
			}
		}
	});
	
	
	public static void main(String[] args) {
			
		fakeGETCHUNK.start();
		fakeCHUNK.start();
				
		System.out.println("< press any key to stop executing >");
		try {System.in.read();} 
			catch (IOException e) {e.printStackTrace();}
		System.out.println("Stopping!");
		System.exit(0);
		
		
	}

}
