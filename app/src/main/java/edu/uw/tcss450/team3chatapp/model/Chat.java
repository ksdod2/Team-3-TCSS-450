package edu.uw.tcss450.team3chatapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Represents the information needed to display and retrieve chat information.
 * @author Kameron Dodd
 * @version 12/4/19
 */
public class Chat implements Serializable, Parcelable {

    /** The ID of the chat. */
    private final int mChatID;
    /** The name of the chat. */
    private final String mName;
    /** The description of the chat. */
    private final String mDesc;
    /** Whether the chat currently has an unread message. */
    private boolean mNewFlag = false;
    /** Whether the user has favorited the chat. */
    private boolean mFavorited = false;

    /**
     * Constructs a new chat.
     * @param tChatID the chat's ID
     * @param tName the chat's name
     * @param tDesc the chat's description
     */
    public Chat(final int tChatID, final String tName, final String tDesc) {
        mChatID = tChatID;
        mName = tName;
        mDesc = tDesc;
    }

    /**
     * Creates a chat from a Parcel.
     * @param in the parcel to use in creating the chat.
     */
    protected Chat(Parcel in) {
        mChatID = in.readInt();
        mName = in.readString();
        mDesc = in.readString();
    }

    /**
     * A Creator for use in creating chats.
     */
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

    /**
     * Gets the chat's ID.
     * @return the chat ID
     */
    public int getChatID() { return  mChatID; }

    /**
     * Gets the chat's name.
     * @return the chat name
     */
    public String getName() { return mName; }

    /**
     * Gets the chat's description.
     * @return the chat's description
     */
    public String getDescription() { return mDesc; }

    /**
     * Returns <code>true</code> if the chat has new messages.
     * @return whether the chat has new messages
     */
    boolean hasNew() { return mNewFlag; }

    /**
     * Returns <code>true</code> if the chat is favorited.
     * @return whether the chat has new messages.
     */
    public boolean isFavorited() { return mFavorited; }

    /**
     * Sets whether or not this chat has new messages.
     * @param tNewFlag whether the chat has new messages or not
     */
    public void setNew(final boolean tNewFlag) { mNewFlag = tNewFlag; }

    /**
     * Sets a chat's favorite status.
     * @param tFavoriteFlag whether the chat is a favorite or not
     */
    void setFavorited(final boolean tFavoriteFlag) { mFavorited = tFavoriteFlag;}

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
