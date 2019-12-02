package za.co.wethinkcode.helpers;

import za.co.wethinkcode.exceptions.FixMessageException;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class FixMessageValidator {

  public static void validateFixVersion(String fixVersion)
    throws FixMessageException {

    if (!fixVersion.equals("FIX.4.0"))
      throw new FixMessageException("Invalid FIX version, should be \"FIX.4.0\".");
  }

  public static void validateBodyLength(String bodyLength)
    throws FixMessageException {

    try {

      Integer.parseInt(bodyLength);
    }
    catch (NumberFormatException e) {

      throw new FixMessageException("Invalid FIX body length type, should be numeric.");
    }
  }

  public static void validateMessageType(String messageType)
    throws FixMessageException {

    if (!messageType.equals("D") && !messageType.equals("8"))
      throw new FixMessageException("Invalid FIX message type, should be either \"D\" or \"8\".");
  }

  public static void validateID(String sourceID, String targetID)
    throws FixMessageException {

    if (sourceID.length() != 6)
      throw new FixMessageException("Invalid sourceID length, should be 6.");

    if (targetID.length() != 6)
      throw new FixMessageException("Invalid targetID length, should be 6.");
  }

  public static void validateMessageSeqNum(String messageSeqNum)
    throws FixMessageException {

    if (!messageSeqNum.equals("1"))
      throw new FixMessageException("Invalid FIX message sequence number, should be \"1\".");
  }

  // This might not be available on Java 7, with Java 7, to do this, you need
  // external dependencies, i.e. non-vanilla.
  public static void validateMessageTimeSent(String messageTimeSent)
    throws FixMessageException {

    try {

      DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.SSS");
      LocalDateTime.from(f.parse(messageTimeSent));
    }
    catch (
      IllegalArgumentException |
      DateTimeException e
    ) {

      throw new FixMessageException(e.getMessage());
    }
  }

  public static void validateBrokerOrderID(String brokerOrderID)
    throws FixMessageException {

    // Can't really validate this, but here's the stub.
  }

  public static void validateMarketOrderID(String marketOrderID)
    throws FixMessageException {

    // Can't really validate this, but here's the stub.
  }

  public static void validateExecID(String execID)
    throws FixMessageException {

    if (!execID.equals("0"))
      throw new FixMessageException("Invalid FIX execID value, " +
          "should be \"0\".");
  }

  public static void validateExecTransType(String execTransType)
    throws FixMessageException {

    if (!execTransType.equals("3"))
      throw new FixMessageException("Invalid FIX execTransType value, " +
          "should be \"3\".");
  }

  public static void validateOrderHandling(String orderHandling)
    throws FixMessageException {

    if (!orderHandling.equals("1"))
      throw new FixMessageException("Invalid FIX order handling value, " +
          "should be \"1\".");
  }

  public static void validateSymbol(String symbol)
    throws FixMessageException {

    // if (symbol.length() != 3)
    //   throw new FixMessageException("Invalid symbol length, should be 3.");
  }

  public static void validateBuyOrSell(String buyOrSell)
    throws FixMessageException {

    if (!buyOrSell.equals("1") && !buyOrSell.equals("2"))
      throw new FixMessageException("Invalid buy or sell option, " +
        "buy should be \"1\", sell should be \"2\".");
  }

  public static void validateAmount(String amount)
      throws FixMessageException {

    try {

      Integer.parseInt(amount);
    }
    catch (NumberFormatException e) {

      throw new FixMessageException(e.getMessage());
    }
  }

  public static void validateOrderType(String orderType)
    throws FixMessageException {

    if (!orderType.equals("1"))
      throw new FixMessageException("Invalid FIX order type, should be \"1\".");
  }

  public static void validateOrderStatus(String orderStatus)
    throws FixMessageException {

    if (!orderStatus.equals("2") && !orderStatus.equals("8"))
      throw new FixMessageException("Invalid order status option, " +
        "filled should be \"2\", rejected should be \"8\".");
  }

  public static void validateAvgFilledPrice(String avgFilledPrice)
    throws FixMessageException {

    try {

      Float.parseFloat(avgFilledPrice);
    }
    catch (NumberFormatException e) {

      throw new FixMessageException(e.getMessage());
    }
  }

  public static void validateChecksumType(String checksum)
    throws FixMessageException {

    try {

      Integer.parseInt(checksum);
    }
    catch (NumberFormatException e) {

      throw new FixMessageException(e.getMessage());
    }
  }

  public static void validateCheckSum(String messageWithoutChecksum, String targetChecksum)
    throws FixMessageException {

    boolean checksumChecksOut = targetChecksum.equals(CheckSum.generateCheckSum(messageWithoutChecksum));

    if (!checksumChecksOut)
      throw new FixMessageException("Invalid FIX message checksum! Abort.");
  }
}
