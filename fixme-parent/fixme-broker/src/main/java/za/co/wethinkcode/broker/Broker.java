package za.co.wethinkcode.broker;

import lombok.Data;
import za.co.wethinkcode.ExecutionReportDecoded;
import za.co.wethinkcode.FixMessage;
import za.co.wethinkcode.exceptions.FixMessageException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

@Data
public class Broker {

    // TODO maybe maintain one instance of the Scanner and PrintWriter

    private String brokerId;
    private final int PORT = 5000;
    private final String HOST = "127.0.0.1";
    private Socket socket;
    private Scanner fromRouter;
    private MarketInstruments marketInstruments;

    public Broker() throws UnknownHostException, IOException {
        socket = new Socket(HOST, PORT);
        this.fromRouter = new Scanner(socket.getInputStream());
        brokerId = this.fromRouter.nextLine();
        System.out.println("Connection to router has been established\n" + "Allocated ID: " + brokerId + "\n");
        marketInstruments = new MarketInstruments();
        System.out.println("Awaiting market connections... ");
    }

    public Broker(int simulationId) throws IOException{
        socket = new Socket(HOST, PORT);
        // TODO: Might need to create the scanner object here as well.
        brokerId = processResponse();
        System.out.println("Thread " + simulationId + ":\n" + "Connection to router has been established\n" + "Allocated ID: " + brokerId + "\n");
    }

    public void sendMessage(Transaction t) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        FixMessage message;
        try {
            message = new FixMessage(
                    brokerId,
                    t.getMarketId(),
                    t.getInstrument(),
                    t.getBuyOrSell().equals("buy") ? "1" : "2",
                    t.getOrderQty()
            );
            out.println(message.toString());
        }catch (FixMessageException e) {
            System.out.println(e.getMessage());
        }
    }

    public String processResponse() throws IOException {

        String line;

          line = fromRouter.nextLine();

        try {
            ExecutionReportDecoded executionReport = new ExecutionReportDecoded(line);
            System.out.println("Market "
                            + executionReport.getSourceID()
                           + (executionReport.getOrderStatus().equals("2") ? " ACCEPTED " : " REJECTED ")
                    + "broker "
                    + executionReport.getTargetID()
                    + "'s request to "
                    + (executionReport.getBuyOrSell().equals("1") ? "buy " : "sell ")
                    + executionReport.getSymbol());
           // marketInstruments.updateQuantities(executionReport);

        } catch (FixMessageException e) {
            while (line.contains(",")) {
                marketInstruments.updateMarketInstruments(line);
                line = fromRouter.nextLine();
            }
            marketInstruments.printMarketInstruments(0);
        }
        return line;
    }

    public void reestablishConnection() {


        for (int i = 1; i <= 5 ; i++) {
            try {
                System.out.println("Connection to router lost attempting to reconnect ... "
                        + String.format("Attempt (%d) out of 5\\n", i));
                try{Thread.sleep(3000);}catch(Exception exception){}
                setSocket(new Socket(getHOST(), getPORT()));
                setFromRouter(new Scanner(socket.getInputStream()));
                setBrokerId(fromRouter.nextLine());
                System.out.println("Connection to router has been established\n" + "Allocated ID: "
                        + getBrokerId() + "\n");
                System.out.println("Awaiting market connections... ");
                break;
            } catch (IOException ioException) {
                System.out.println("Oops couldn't connect attempting again in 5 seconds.\n");
                if (i == 5) {
                    System.out.println("Failed to reconnect.");
                    System.exit(1);
                }
            }
        }

    }




}