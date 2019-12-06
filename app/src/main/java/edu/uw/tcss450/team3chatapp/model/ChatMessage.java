package edu.uw.tcss450.team3chatapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Class to represent a single chat message.
 * @author Kameron Dodd
 * @version 10/30/19
 */
public class ChatMessage implements Serializable, Parcelable {

    /** Whether the message was sent by the current user. */
    private final boolean amSender;
    /** The username of the sender. */
    private final String mSender;
    /** The timestamp of when the message was received by the web service. */
    private final String mTimestamp;
    /** The content of the message. */
    private final String mMessage;

    /**
     * Constructs a new chat message.
     * @param isSender whether the message was sent by the current user
     * @param tSender the username of the sender
     * @param tTimestamp the timestamp on the message
     * @param tMessage the message content
     */
    public ChatMessage(final boolean isSender, final String tSender,
                       final String tTimestamp, final String tMessage) {
        amSender = isSender;
        mSender = tSender;
        mTimestamp = tTimestamp.substring(0, tTimestamp.indexOf('.'));
        mMessage = tMessage;
    }

    /**
     * Constructs a new chat message from a Parcel.
     * @param in the parcel to use in creating the chat
     */
    protected ChatMessage(Parcel in) {
        amSender = in.readInt() == 1;
        mSender = in.readString();
        mTimestamp = in.readString();
        mMessage = in.readString();
    }

    /** A Creator for use with producing chats. */
    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    /**
     * Gets the username of the message's sender.
     * @return the sender's username
     */
    String getSender() { return mSender; }

    /**
     * Gets the timestamp on the message.
     * @return the message's timestamp
     */
    String getTimestamp() { return mTimestamp; }

    /**
     * Gets the content of the message.
     * @return
     */
    public String getMessage() { return mMessage; }

    /**
     * Returns <code>true</code> if the message was sent by the current user.
     * @return whether the current user sent this message
     */
    boolean amSender() { return amSender; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(amSender ? 1 : 0);
        parcel.writeString(mSender);
        parcel.writeString(mTimestamp);
        parcel.writeString(mSender);
    }
}
