package za.co.wethinkcode.market;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import za.co.wethinkcode.ExecutionReportDecoded;
import za.co.wethinkcode.FixMessage;
import za.co.wethinkcode.SingleOrderDecoded;
import za.co.wethinkcode.exceptions.FixMessageException;
import za.co.wethinkcode.helpers.BroadcastEncoder;
import za.co.wethinkcode.helpers.Instrument;
import za.co.wethinkcode.instruments.InstrumentReader;

public class Market {
	
	private String marketId;
	private String HOST = "127.0.0.1";
	private int PORT = 5001;
	private Socket connection = null;
	List<Instrument> instruments = null;
	Map<String, Instrument> mappedInstruments = new HashMap<String, Instrument>();
			
	public Market() throws Exception {
    	connection = new Socket(HOST, PORT);
    	Scanner in = new Scanner(connection.getInputStream());
    	PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
    	
    	marketId = in.nextLine();
    	System.out.println("Market was allocated ID: " + marketId);
    	// Read instruments
    	instruments = InstrumentReader.getInstruments();
    	mapInstruments();
    	// Broadcast instruments
    	String encodedBroadcast = BroadcastEncoder.encode(marketId, instruments, true);
    	out.println(encodedBroadcast);
	}
	
	public void sendResponse(String message) throws IOException {
		PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
		
		// handle message and send back FIX to connection
		try {
			SingleOrderDecoded fix = new SingleOrderDecoded(message);
			FixMessage executionReport = null;			
			Instrument query = mappedInstruments.get(fix.getSymbol());
			
			if (query == null) {
				executionReport = new FixMessage(
						marketId,
						fix.getSourceID(),
						"8",
						fix.getSymbol(),
						fix.getBuyOrSell(),
						fix.getOrderAmount(),
						"0",
						"0"
				);
			} else {
				
				String buyOrSell = fix.getBuyOrSell();
				
				if (buyOrSell.equals("1")) {
					
					// Requesting buy
					
					int qtyReq = Integer.parseInt(fix.getOrderAmount());
					
					if (query.reserveQty < qtyReq) {
						// Reject
						executionReport = new FixMessage(
								marketId,
								fix.getSourceID(),
								"8",
								fix.getSymbol(),
								fix.getBuyOrSell(),
								fix.getOrderAmount(),
								"0",
								"0"
						);
					} else {
						// Execute
						executionReport = new FixMessage(
								marketId,
								fix.getSourceID(),
								"2",
								fix.getSymbol(),
								fix.getBuyOrSell(),
								fix.getOrderAmount(),
								fix.getOrderAmount(),
								"42.42"
						);
						query.reserveQty -= qtyReq;
					}				
				} else {
					// Requesting sell
					
					int qtyReq = Integer.parseInt(fix.getOrderAmount());
					
					// Always execute
					
					executionReport = new FixMessage(
							marketId,
							fix.getSourceID(),
							"2",
							fix.getSymbol(),
							fix.getBuyOrSell(),
							fix.getOrderAmount(),
							fix.getOrderAmount(),
							"42.42"
					);					
					
					query.reserveQty += qtyReq;
				}
			}


			
			out.println(executionReport);
			
		} catch (FixMessageException e) {
			e.printStackTrace();
		}
	}
	
	public String getFixMessage() throws IOException {
		Scanner in = new Scanner(connection.getInputStream());			
		return in.nextLine();
	}

	private void mapInstruments() {
		for(Instrument i : instruments) {
			mappedInstruments.put(i.instrument, i);
		}
	}
}
