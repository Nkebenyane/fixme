package za.co.wethinkcode.broker;

import lombok.Data;
import za.co.wethinkcode.App;
import za.co.wethinkcode.ExecutionReportDecoded;
import za.co.wethinkcode.helpers.BroadcastDecoder;
import za.co.wethinkcode.helpers.Instrument;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MarketInstruments {

    private  Map<String, List<Instrument>> instruments = new HashMap<>();

    public  void updateMarketInstruments(String broadcastMessage) {
        String marketId = BroadcastDecoder.getMarketId(broadcastMessage);

        if (BroadcastDecoder.isOpenMarket(broadcastMessage)) {
             instruments.put(marketId, BroadcastDecoder.decode(broadcastMessage));
        }
        else {
            System.out.println("Market " + marketId + " is closed.");
            instruments.remove(marketId);
            if (!App.dataReceived)
                App.flipSwitch();
        }
    }

    public void printMarketInstruments(int simulationId) {
        instruments.forEach((marketId, instruments) -> {
            StringBuilder s = new StringBuilder();
            for (Instrument i : instruments) {
                s.append(i.instrument).append(", ");
            }
            System.out.println(
                    (simulationId > 0 ? "Thread " + simulationId + " Market Available:\n" : "" ) +
                    "Market " + marketId + " trades " + s.toString().replaceAll(", $", "."));

        });
    }

    public  void updateQuantities(ExecutionReportDecoded executionReport) {
        for (Instrument i : instruments.get(executionReport.getSourceID())) {
            if (i.instrument.equals(executionReport.getSymbol()))  {
                if (executionReport.getBuyOrSell().equals("1")) {
                    i.reserveQty += Integer.parseInt(executionReport.getFilledAmount());
                }
            }
        }
    }
}
