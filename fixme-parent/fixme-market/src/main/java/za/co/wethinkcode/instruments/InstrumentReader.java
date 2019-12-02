package za.co.wethinkcode.instruments;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import za.co.wethinkcode.helpers.Instrument;

public class InstrumentReader {
	
	private static String[] readableMarkets = {"Market1", "Market2", "Market3", "Market4", "Market5"};
	private static final int marketChoice = ThreadLocalRandom.current().nextInt(0, readableMarkets.length);
	
	public static List<Instrument> getInstruments() throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		
		String jsonInput = null;
		try {
			jsonInput = readFile();					
		} catch (IOException e) {
			System.out.println("Unable to get jsonInput from file");
			List<Instrument> toReturn = new ArrayList<Instrument>();
			toReturn.add(new Instrument(1, "USD", 1500));
			return toReturn;
		}

		List<Instrument> instruments = mapper.readValue(jsonInput, new TypeReference<List<Instrument>>(){});
						
		return instruments;
	}	
	
	private static String readFile() throws IOException
	{				
		URL url = Resources.getResource(readableMarkets[marketChoice] + ".json");
		return Resources.toString(url, Charsets.UTF_8);		
	}
}
