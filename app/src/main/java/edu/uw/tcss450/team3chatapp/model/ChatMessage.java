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
    private final String mSender;
    private final String mTimestamp;
    private final String mMessage;

    public ChatMessage(final String tSender, final String tTimestamp, final String tMessage) {
        mSender = tSender;
        mTimestamp = tTimestamp.substring(0, tTimestamp.indexOf('.'));
        mMessage = tMessage;
    }

    protected ChatMessage(Parcel in) {
        mSender = in.readString();
        mTimestamp = in.readString();
        mMessage = in.readString();
    }

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

    public String getSender() { return mSender; }

    public String getTimestamp() { return mTimestamp; }

    public String getMessage() { return mMessage; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mSender);
        parcel.writeString(mTimestamp);
        parcel.writeString(mSender);
    }
}
