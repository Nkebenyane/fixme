package za.co.wethinkcode.helpers;

import java.util.List;

public abstract class BroadcastEncoder {

	public static String encode(String marketId, List<Instrument> instruments, boolean open) {
		
		if (!open) {
			return closedMarket(marketId);
		}
		
		StringBuilder b = new StringBuilder();
		b.append(openMarket(marketId));
		b.append(", ");
		
		for (Instrument i : instruments) {
			b.append(i.instrument);
			b.append(", ");
		}
		
		
		// regex to remove trailing ", "
		return b.toString().replaceAll(", $", "");
	}
	
	private static String openMarket(String marketId) {
		return marketId + ", OPEN";
	}
	
	private static String closedMarket(String marketId) {
		return marketId + ", CLOSED";
	}
	
}
