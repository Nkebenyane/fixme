package za.co.wethinkcode;

// This class doesn't validate and assumes that the string passed in is a valid
// execution report FIX message.

import za.co.wethinkcode.exceptions.FixMessageException;
import za.co.wethinkcode.helpers.FixMessageValidator;

public class ExecutionReportDecoded {

  private String[] splitOrder;

  private String fixVersion;
  private String bodyLength;
  private String messageType;
  private String sourceID;
  private String targetID;
  private String messageSeqNum;
  private String messageTimeSent;

  private String marketOrderID;
  private String execID;
  private String execTransType;
  private String orderStatus;
  private String symbol;
  private String buyOrSell;
  private String orderAmount;
  private String filledAmount;
  private String avgFilledPrice;

  private String checksum;

  public ExecutionReportDecoded(String executionReport)
    throws FixMessageException {

    this.splitOrder = executionReport.split("\\|");

    if (this.splitOrder.length != 17)
      throw new FixMessageException("Couldn't decode single order FIX message, " +
          "invalid message length.");

    this.fixVersion = this.getActualTagInfo(this.splitOrder[0]);
    this.bodyLength = this.getActualTagInfo(this.splitOrder[1]);
    this.messageType = this.getActualTagInfo(this.splitOrder[2]);
    this.sourceID = this.getActualTagInfo(this.splitOrder[3]);
    this.targetID = this.getActualTagInfo(this.splitOrder[4]);
    this.messageSeqNum = this.getActualTagInfo(this.splitOrder[5]);
    this.messageTimeSent = this.getActualTagInfo(this.splitOrder[6]);

    this.marketOrderID = this.getActualTagInfo(this.splitOrder[7]);
    this.execID = this.getActualTagInfo(this.splitOrder[8]);
    this.execTransType = this.getActualTagInfo(this.splitOrder[9]);
    this.orderStatus = this.getActualTagInfo(this.splitOrder[10]);
    this.symbol = this.getActualTagInfo(this.splitOrder[11]);
    this.buyOrSell = this.getActualTagInfo(this.splitOrder[12]);
    this.orderAmount = this.getActualTagInfo(this.splitOrder[13]);
    this.filledAmount = this.getActualTagInfo(this.splitOrder[14]);
    this.avgFilledPrice = this.getActualTagInfo(this.splitOrder[15]);

    this.checksum = this.getActualTagInfo(this.splitOrder[16]);

    FixMessageValidator.validateFixVersion(this.fixVersion);
    FixMessageValidator.validateBodyLength(this.bodyLength);
    FixMessageValidator.validateMessageType(this.messageType);
    FixMessageValidator.validateID(this.sourceID, this.targetID);
    FixMessageValidator.validateMessageSeqNum(this.messageSeqNum);
    FixMessageValidator.validateMessageTimeSent(this.messageTimeSent);

    FixMessageValidator.validateMarketOrderID(this.marketOrderID);
    FixMessageValidator.validateExecID(this.execID);
    FixMessageValidator.validateExecTransType(this.execTransType);
    FixMessageValidator.validateOrderStatus(this.orderStatus);
    FixMessageValidator.validateSymbol(this.symbol);
    FixMessageValidator.validateBuyOrSell(this.buyOrSell);
    FixMessageValidator.validateAmount(this.orderAmount);
    FixMessageValidator.validateAmount(this.filledAmount);
    FixMessageValidator.validateAvgFilledPrice(this.avgFilledPrice);

    FixMessageValidator.validateChecksumType(this.checksum);
  }

  // Header Info
  public String getFixVersion() { return this.fixVersion; }
  public String getBodyLength() { return this.bodyLength; }
  public String getMessageType() { return this.messageType; }
  public String getSourceID() { return this.sourceID; }
  public String getTargetID() { return this.targetID; }
  public String getMessageSeqNum() { return this.messageSeqNum; }
  public String getMessageTimeSent() { return this.messageTimeSent; }

  // Body Info
  public String getMarketOrderID() { return this.marketOrderID; }
  public String getExecID() { return this.execID; }
  public String getExecTransType() { return this.execTransType; }
  public String getOrderStatus() { return this.orderStatus; }
  public String getSymbol() { return this.symbol; }
  public String getBuyOrSell() { return this.buyOrSell; }
  public String getOrderAmount() { return this.orderAmount; }
  public String getFilledAmount() { return this.filledAmount; }
  public String getAvgFilledPrice() { return this.avgFilledPrice; }

  // Footer Info
  public String getChecksum() { return this.checksum; }

    // Extra
  public String getMessageWithoutChecksum() {

    String messageWithoutChecksum = "";

    for (int i = 0; i < this.splitOrder.length - 1; i++) {

      messageWithoutChecksum += this.splitOrder[i] + "|";
    }

    return messageWithoutChecksum;
  }

  private String getActualTagInfo(String fixTagString)
    throws FixMessageException {

    String splitString[] = fixTagString.split("=");

    if (splitString.length != 2)
      throw new FixMessageException("Couldn't decode single order FIX message, " +
          "missing or extra \"=\"'s.");

    return splitString[1];
  }
}
