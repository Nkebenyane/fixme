package za.co.wethinkcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import za.co.wethinkcode.broker.*;

public class App
{
    public volatile static boolean dataReceived = false;
    public  static MarketInstruments marketInstruments ;
    public static void main( String[] args ) throws Exception {
        // Assume interactive mode

        //Establishes a connection to server
        if (args.length == 1 && args[0].equals("-i")) {
            Broker b = new Broker();

            marketInstruments = b.getMarketInstruments();
            Thread t1 = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            b.processResponse();
                            System.out.println("Enter transaction message: ");
                            if (!dataReceived)
                                flipSwitch();
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        } catch (NoSuchElementException e) {
                            if (!dataReceived)
                              b.reestablishConnection();
                         }
                    }
                }
            });
            t1.start();
            // Loop infinitely for input and process
            while (true) {

               // System.out.println(dataReceived);
                if (dataReceived) {

                    try {
                        // Get user input

                        String input = getInput();
                        // Build new transaction
                        Transaction t = Transaction.buildTransaction(input);
                        // Send transaction to router
                        b.sendMessage(t);
                        // Handle server response from "market"
                        System.out.println("Waiting for response...");
                        //System.out.println(b.processResponse());


                    } catch (InvalidInputException e) {
                        System.out.println(e.getMessage());
                        System.out.println("Enter transaction message: ");
                    } catch(IOException | NoSuchElementException e) {
                        System.out.println(e.getMessage());
                    }
                    flipSwitch();
                }
            }
        } else if (args.length == 4
                && args[0].equals("-s")
                && args[1].matches("\\d+")
                && args[2].equals("-cycles")
                && args[3].matches("\\d+")) {
            try {
                int brokers = Integer.parseInt(args[1]);
                ExecutorService pool = Executors.newFixedThreadPool(brokers);
                int i = 0;


                while (i < brokers) {
                    pool.execute(new Simulation(Integer.parseInt(args[3]), i + 1));
                    i++;
                }
                pool.shutdown();
            } catch (IOException e) {
                System.out.println(e);
            }
        } else {
            System.out.println("Usage:\n\t" +
                    "java -jar [*.jar] -i | -s [number of brokers to simulate] -cycles [maximum number of cycles]\n" +
                    "Flags:\n\t-i <interactive mode>\n" +
                    "\t-s <simulation mode for simulated broker>\n" +
                    "\t-cycles <maximum number of cycles>");
        }
    }

    private static String getInput() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            return reader.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static boolean isSimulationMode(String []args) {
        if (args.length == 4) {

            return true;
        }
        return false;
    }

    public synchronized static void flipSwitch() {
        dataReceived = !dataReceived;
       // System.out.println(dataReceived);
    }

}