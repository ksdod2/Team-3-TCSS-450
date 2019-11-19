package edu.uw.tcss450.team3chatapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Represents the information needed to display and retrieve chat information.
 * @author Kameron Dodd
 * @version 10/30/19
 */
public class Chat implements Serializable, Parcelable {
    private final int mChatID;
    private final String mName;
    private final String mDesc;
    private final boolean mConfirmation;

    public Chat(final int tChatID, final String tName, final String tDesc, final boolean tConf) {
        mChatID = tChatID;
        mName = tName;
        mDesc = tDesc;
        mConfirmation = tConf;
    }

    protected Chat(Parcel in) {
        mChatID = in.readInt();
        mName = in.readString();
        mDesc = in.readString();
        mConfirmation = (in.readByte() != 0);
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public int getChatID() { return  mChatID; }
    public String getName() { return mName; }
    public String getDescription() { return mDesc; }
    public boolean getConfirmation() { return mConfirmation; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mDesc);
        parcel.writeInt(mChatID);
        parcel.writeInt(mConfirmation ? 1 : 0);
    }
}
