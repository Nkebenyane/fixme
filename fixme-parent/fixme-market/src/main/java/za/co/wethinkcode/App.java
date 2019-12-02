package za.co.wethinkcode;

import java.io.IOException;
import java.net.UnknownHostException;

import za.co.wethinkcode.market.Market;

public class App {
    public static void main( String[] args ) throws UnknownHostException, IOException, Exception {
    	Market m = new Market();
    	
    	while (true) {
    		String message = m.getFixMessage();
    		m.sendResponse(message);
    	}
    }
}

//Socket s = new Socket("127.0.0.1", 5001);
//Scanner in = new Scanner(s.getInputStream());
//PrintWriter out = new PrintWriter(s.getOutputStream(), true);

// Should be 123456
//System.out.println("Received ID: " + in.nextLine());

// this will only process one broker at a time
//while (in.hasNextLine()) {
//	String brokerId = in.nextLine();
//	System.out.println("Received from router: " + brokerId);
//	
//	out.println(brokerId);
//	System.out.println("Send the message back");
//}
