package edu.uw.tcss450.team3chatapp.model;

/**
 * Class to represent a single chat message.
 * @author Kameron Dodd
 * @version 10/30/19
 */
public class ChatMessage {
    private final String mSender;
    private final String mTimestamp;
    private final String mMessage;

    public ChatMessage(final String tSender, final String tTimestamp, final String tMessage) {
        mSender = tSender;
        mTimestamp = tTimestamp;
        mMessage = tMessage;
    }

    public String getSender() { return mSender; }

    public String getTimestamp() { return mTimestamp; }

    public String getMessage() { return mMessage; }
}
