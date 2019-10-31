package edu.uw.tcss450.team3chatapp.model;

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
