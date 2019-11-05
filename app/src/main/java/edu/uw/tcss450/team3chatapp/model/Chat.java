package edu.uw.tcss450.team3chatapp.model;

/**
 * Represents the information needed to display and retrieve chat information.
 * @author Kameron Dodd
 * @version 10/30/19
 */
public class Chat {
    private final int mChatID;
    private final String mName;
    // May want other properties such as a description, image, ect in the future

    public Chat(final int tChatID, final String tName) {
        mChatID = tChatID;
        mName = tName;
    }

    public int getChatID() { return  mChatID; }
    public String getName() { return mName; }
}
