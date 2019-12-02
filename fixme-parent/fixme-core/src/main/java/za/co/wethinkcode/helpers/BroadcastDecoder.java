package za.co.wethinkcode.helpers;

import java.util.ArrayList;
import java.util.List;

public abstract class BroadcastDecoder {

	public static List<Instrument> decode(String encodedBroadcast) {
		
		List<Instrument> instruments = new ArrayList<Instrument>();
		
		String[] split = encodedBroadcast.split(", ");
		
		for (int i = 2; i < split.length; i++) {
			Instrument instrument = new Instrument();
			instrument.id = i - 1;
			instrument.instrument = split[i];
			
			instruments.add(instrument);			
		}		
		
		return instruments;
		
	}
	
	public static boolean isOpenMarket(String encodedBroadcast) {
		
		String[] split = encodedBroadcast.split(", ");
		
		if (split.length < 2) {
			return false;
		}
		
		if (split[1].equals("OPEN")) {
			return true;
		}
		
		return false;
	}
	
	public static String getMarketId(String encodedBroadcast) {
		String[] split = encodedBroadcast.split(", ");
		
		if (split.length == 0) {
			return "";
		}
		
		return split[0];
	}
	
}
