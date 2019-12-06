package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A ViewModel for a list of chats associated with a user's account.
 * @author Kameron Dodd
 * @version 12/4/19
 */
public class ChatListViewModel extends ViewModel {

    /** Reference to the singular instance of this ViewModel. */
    private static ChatListViewModel mInstance;
    /** The list of chats. */
    private List<Chat> mChats = new ArrayList<>();
    /** The MutableLiveData to allow observation of the chats. */
    private MutableLiveData<List<Chat>> chats;

    /** Private constructor to implement factory design pattern. */
    private ChatListViewModel() { chats = new MutableLiveData<>(); }

    /**
     * Method to get access to the live data to add observers.
     * @return the LiveData object containing the connections
     */
    public LiveData<List<Chat>> getCurrentChats() { return chats; }

    /**
     * Adds a new chat to the list of chats.
     * @param tChat the connection to add
     */
    public void addChat(final Chat tChat) {
        // Add the new chat
        mChats.add(tChat);
        // Update the LiveData to alert to observers
        chats.setValue(mChats);
    }

    /**
     * Removes a new chat from the list of chats.
     * @param tChat the connection to remove
     */
    public void removeChat(final Chat tChat) {
        // Remove the chat
        mChats.remove(tChat);
        // Update the LiveData to alert to observers
        chats.setValue(mChats);
    }

    /**
     * Replaces the list of chats with a new one.
     * @param tChats list of chats to use
     */
    public void setChats(final List<Chat> tChats) {
        mChats = tChats;
        chats.setValue(mChats);
    }

    /**
     * Sets a chat in the list of chats to recognize unread messages have occurred.
     * @param chatID the ID of the chat to flag
     * @param hasUnread whether the chat now has unread messages
     */
    public void setUnread(final int chatID, final boolean hasUnread) {
        for(Chat c : mChats) {
            if(c.getChatID() == chatID)
                c.setNew(hasUnread);
        }
        chats.setValue(mChats);
    }

    /**
     * Sets a chat in the list of chats to recognize that its favorite status changed.
     * @param chatID the ID of the chat to flag
     * @param isFav whether the chat is favorited
     */
    public void setFavorite(final int chatID, final boolean isFav) {
        for(Chat c : mChats) {
            if(c.getChatID() == chatID)
                c.setFavorited(isFav);
        }
        chats.setValue(mChats);
    }

    /**
     * Factory method to provide the factory that gives access to the singleton instance.
     * @return the factory that provides the instance of this ViewModel
     */
    public static ViewModelProvider.Factory getFactory() {
        return new ViewModelProvider.Factory() {

            @NonNull
            @Override
            public ChatListViewModel create(@NonNull Class modelClass) {
                if (mInstance == null)
                    mInstance = new ChatListViewModel();
                return mInstance;
            }
        };
    }

}
