package za.co.wethinkcode.broker;

import lombok.Data;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Simulation implements  Runnable{

    private Broker broker;
    private int simulationId;
    private static Transaction transaction;
    private static String transactionString;
    private static String marketId;
    private static String buyOrSell;
    private static String instrument;
    private static String quantity;
    private MarketInstruments marketInstruments;
    private int cycles;

    public Simulation(int cycles, int id) throws IOException {
        simulationId = id;
        this.cycles = cycles;
        broker = new Broker(id);
        marketInstruments = new MarketInstruments();
    }

    public static void generateTransaction() {

        marketId = getRandomMarket();
        instrument = getRandomInstrument(marketId);
        buyOrSell = !brokerHasInstrument(instrument)
                ? "buy"
                : ThreadLocalRandom.current().nextInt(0, 10) > 5
                ? "buy"
                : "sell";
        quantity = getRandomQuantity(buyOrSell);

        try {
            transactionString = marketId + " "
                    + buyOrSell + " "
                    + instrument + " "
                    + quantity;
            transaction = Transaction.buildTransaction(transactionString);
        } catch (InvalidInputException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getRandomMarket() {
        //TODO:
        return "123456";
    }

    public static String getRandomInstrument(String marketId) {
        // TODO:
        return "XAU";
    }

    public static boolean brokerHasInstrument(String instrument) {
        //TODO:
        return false;
    }

    public static String getRandomQuantity(String buyOrSell) {
        //TODO:
        if (buyOrSell.equals("buy")) {
            return "100";
        } else  {
            return "200";
        }
    }


    public void awaitBroadCast() {
        String line;
        try {
            Scanner in = new Scanner(broker.getSocket().getInputStream());
            System.out.println("Thread " + simulationId + ":\nWaiting for Markets to Connect ...\n");
            while (!in.hasNextLine()) /* Wait for markets to connect */;
            line = in.nextLine();
//            marketInstruments.updateMarketInstruments(line);
//            marketInstruments.printMarketInstruments(simulationId);
            //while (true);
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {

        String response;
        int i = 0;

        awaitBroadCast();
        while (i < cycles) {
            generateTransaction();
            try {
                System.out.println("Thread " + simulationId + " Request to Market:\n" + transactionString + "\n");
                broker.sendMessage(transaction);
                response = broker.processResponse();
                System.out.println("Thread " + simulationId + "Response from Market:\n" + response + "\n");

            } catch (IOException | NoSuchElementException e) {
                System.out.println("Thread " + simulationId + " Error:\n" + e.getMessage() + "\n");
            }
            i++;
        }
    }



}
