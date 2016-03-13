package file_utils;

import java.util.Random;

public class RandomDelay {
	public static int randomInt(int min, int max){
		Random rand = new Random();
		int randNum = rand.nextInt((max - min) + 1) + min; 
		return randNum;
	}
}
