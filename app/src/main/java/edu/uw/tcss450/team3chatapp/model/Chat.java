package edu.uw.tcss450.team3chatapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

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
    private boolean mNewFlag = false;

    public Chat(final int tChatID, final String tName, final String tDesc) {
        mChatID = tChatID;
        mName = tName;
        mDesc = tDesc;
    }

    protected Chat(Parcel in) {
        mChatID = in.readInt();
        mName = in.readString();
        mDesc = in.readString();
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
    public boolean hasNew() { return mNewFlag; }

    public void setNew(final boolean tNewFlag) { mNewFlag = tNewFlag; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mDesc);
        parcel.writeInt(mChatID);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof  Chat))
            return false;
        return mChatID == ((Chat) obj).mChatID;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(mChatID);
    }
}
