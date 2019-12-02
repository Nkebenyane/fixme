package za.co.wethinkcode.helpers;

import java.util.concurrent.ThreadLocalRandom;

public class IdGenerator {
	
	public static String generateId(int digitAmount) {
		
		// Copied Evert's ID generator into helper
		
		final String upperAlpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";		
		String brokerOrderID = "";
		for (int i = 0; i < digitAmount; i++) {
			if (i % 2 == 0) {
				int randomAlphaIndex = ThreadLocalRandom.current().nextInt(0, 25 + 1);
				brokerOrderID += upperAlpha.charAt(randomAlphaIndex);
			}
			else {
				int randomInteger = ThreadLocalRandom.current().nextInt(1, 9 + 1);
				brokerOrderID += Integer.toString(randomInteger);
			}
		}
		return brokerOrderID;
	}

}
