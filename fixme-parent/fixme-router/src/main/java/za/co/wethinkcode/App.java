package za.co.wethinkcode;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import za.co.wethinkcode.exceptions.FixMessageException;
import za.co.wethinkcode.helpers.*;

public class App {
	private static final Logger logger = LogManager.getLogger(App.class.getName());

	public static void main(String[] args) {
		// Spawn a server socket on a new thread for broker
		Thread t1 = new Thread(new BrokersHandler());
		t1.start();

		// Spawn a server socket on new thread for market
		Thread t2 = new Thread(new MarketsHandler());
		t2.start();
	}
}

class BrokersHandler implements Runnable {

	private static final Logger logger = LogManager.getLogger(BrokersHandler.class.getName());

	@Override
	public void run() {
		try (ServerSocket listener = new ServerSocket(5000)) {
			logger.info("Router is now accepting connections from brokers");
			ExecutorService pool = Executors.newFixedThreadPool(20);
			while (true) {
				pool.execute(new BrokerHandler(listener.accept()));
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}

class MarketsHandler implements Runnable {

	private static final Logger logger = LogManager.getLogger(MarketsHandler.class.getName());

	@Override
	public void run() {
		try (ServerSocket listener = new ServerSocket(5001)) {
			logger.info("Router is now accepting connections from markets");
			ExecutorService pool = Executors.newFixedThreadPool(20);
			while (true) {
				pool.execute(new MarketHandler(listener.accept()));
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}

class MarketHandler implements Runnable {

	private Socket socket;
	private String marketId;
	private Scanner fromMarket;
	private String encodedBroadcast;

	MarketHandler(Socket s) {
		this.socket = s;
	}

	@Override
	public void run() {

		System.out.println("Market Connected: " + this.socket);

		try {

			this.marketId = IdGenerator.generateId(6);
			while (!ActiveConnections.idIsAvailable(this.marketId)) {
				this.marketId = IdGenerator.generateId(6);
			}

			ActiveConnections.addMarket(this.marketId, this.socket);
			this.fromMarket = ActiveConnections.getFromMarket(this.marketId);

			// Sending related market its unique ID.
			ActiveConnections.writeToMarket(this.marketId, this.marketId);

			// Getting connected market instrument list, this should block.
			if (this.fromMarket.hasNextLine()) {
				this.encodedBroadcast = this.fromMarket.nextLine();
			}

			// Adding market instrument list to marketHashMap.
			ActiveConnections.addEncodedBroadcast(this.encodedBroadcast);

			// Notifying brokers that a new market is available.
			ActiveConnections.notifyBrokersOpenMarket(this.encodedBroadcast);

			// Routes FIX execution reports to related brokers. Reading doesn't have
			// to be thread-safe as reading is unique to each thread.
			while (this.fromMarket.hasNextLine()) {

				String line = this.fromMarket.nextLine();

				ExecutionReportDecoded decodedExecutionReport = new ExecutionReportDecoded(line);

				FixMessageValidator.validateCheckSum(decodedExecutionReport.getMessageWithoutChecksum(),
						decodedExecutionReport.getChecksum());

				String brokerID = decodedExecutionReport.getTargetID();

				// Forwarding message to related broker.
				ActiveConnections.writeToBroker(brokerID, line);
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} catch (FixMessageException e) {
			e.printStackTrace(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			ActiveConnections.removeEncodedBroadcast(this.encodedBroadcast);
			ActiveConnections.notifyBrokersClosedMarket(this.encodedBroadcast);
			this.fromMarket.close();
			ActiveConnections.removeMarket(this.marketId);
		}
	}
}

class BrokerHandler implements Runnable {

	private Socket socket;
	private String brokerId;
	private Scanner fromBroker;

	BrokerHandler(Socket s) {
		this.socket = s;
	}

	@Override
	public void run() {

		System.out.println("Broker Connected: " + this.socket);

		try {

			this.brokerId = IdGenerator.generateId(6);
			while (!ActiveConnections.idIsAvailable(this.brokerId)) {
				this.brokerId = IdGenerator.generateId(6);
			}

			ActiveConnections.addBroker(this.brokerId, this.socket);
			this.fromBroker = ActiveConnections.getFromBroker(this.brokerId);

			// Sending related broker its unique ID.
			ActiveConnections.writeToBroker(this.brokerId, this.brokerId);

			// Sending broker the current traded market instruments.
			ActiveConnections.notifyBrokerMarketInstruments(this.brokerId);

			// Routes FIX buy/sell messages to related markets. Reading doesn't have
			// to be thread-safe as reading is unique to each thread.
			while (this.fromBroker.hasNextLine()) {

				String line = this.fromBroker.nextLine();

				SingleOrderDecoded decodedBrokerMessage = new SingleOrderDecoded(line);

				// Validating FIX message checksum.
				FixMessageValidator.validateCheckSum(decodedBrokerMessage.getMessageWithoutChecksum(),
						decodedBrokerMessage.getChecksum());

				String marketID = decodedBrokerMessage.getTargetID();

				// Forwarding message to related market.
				ActiveConnections.writeToMarket(marketID, line);
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} catch (FixMessageException e) {
			e.printStackTrace(System.out);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		} finally {
			this.fromBroker.close();
			ActiveConnections.removeBroker(this.brokerId);
		}
	}
}

// Socket Connectivity Singleton Wrapper.
class SocketConnectivity {

	private PrintWriter toSocket;
	private Scanner fromSocket;

	public SocketConnectivity(Socket socket) throws IOException {

		this.toSocket = new PrintWriter(socket.getOutputStream(), true);
		this.fromSocket = new Scanner(socket.getInputStream());
	}

	// Syncing here as PrintWriter is not thread-safe.
	public synchronized void writeToSocket(String line) {

		this.toSocket.println(line);
	}

	public Scanner getFromSocket() {

		return this.fromSocket;
	}
}

abstract class ActiveConnections {

	private static HashMap<String, SocketConnectivity> brokers = new HashMap<>();
	private static HashMap<String, SocketConnectivity> markets = new HashMap<>();
	private static HashMap<String, String> marketInstruments = new HashMap<>();

	public static boolean idIsAvailable(String id) {

		if (brokers.get(id) == null && markets.get(id) == null) {
			return true;
		}

		return false;
	}

	public static synchronized void addMarket(String marketId, Socket marketSocket) throws IOException {

		markets.put(marketId, new SocketConnectivity(marketSocket));
	}

	public static synchronized void addBroker(String brokerId, Socket brokerSocket) throws IOException {

		brokers.put(brokerId, new SocketConnectivity(brokerSocket));
	}

	public static synchronized void addEncodedBroadcast(String encodedMarketBroadcast) {

		marketInstruments.put(BroadcastDecoder.getMarketId(encodedMarketBroadcast), encodedMarketBroadcast);
	}

	public static synchronized void removeBroker(String brokerId) {

		brokers.remove(brokerId);
	}

	public static synchronized void removeMarket(String marketId) {

		markets.remove(marketId);
	}

	public static synchronized void removeEncodedBroadcast(String encodedBroadcast) {

		String marketId = BroadcastDecoder.getMarketId(encodedBroadcast);
		marketInstruments.remove(marketId);
	}

	// Do not synchronize here, will result in blocking across app.
	public static void writeToBroker(String brokerId, String line) {

		brokers.get(brokerId).writeToSocket(line);
	}

	public static Scanner getFromBroker(String brokerId) {

		return brokers.get(brokerId).getFromSocket();
	}

	// Do not synchronize here, will result in blocking across app.
	public static void writeToMarket(String marketId, String line) {

		markets.get(marketId).writeToSocket(line);
	}

	public static Scanner getFromMarket(String marketId) {

		return markets.get(marketId).getFromSocket();
	}

	// TODO: Test.
	// Broker gets notified when a market disconnects.
	public static void notifyBrokersClosedMarket(String encodedBroadcast) {

		String marketId = BroadcastDecoder.getMarketId(encodedBroadcast);
		String closedMarket = BroadcastEncoder.encode(marketId, null, false);

		brokers.forEach((brokerId, socketConnectivity) -> {

			try {

				writeToBroker(brokerId, closedMarket);
				writeToBroker(brokerId, "\0");

			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});
	}

	// TODO: Test.
	// Broker gets notified when a market connects.
	public static void notifyBrokersOpenMarket(String encodedBroadcast) {

		brokers.forEach((brokerId, socketConnectivity) -> {

			try {

				writeToBroker(brokerId, encodedBroadcast);
				writeToBroker(brokerId, "\0");
			} catch (Exception e) {
				e.printStackTrace(System.out);
			}
		});
	}

	// TODO: Test.
	// Broker receives market instruments upon connection to router.
	public static void notifyBrokerMarketInstruments(String brokerId) {

		try {

			marketInstruments.forEach((marketId, encodedBroadcast) -> {
				writeToBroker(brokerId, encodedBroadcast);
			});
			if (marketInstruments.size() > 0)
				writeToBroker(brokerId, "\0");
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}