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
 * @version 11/20/19
 */
public class ChatListViewModel extends ViewModel {

    // Using factory design pattern to ensure only one instance across application
    private static ChatListViewModel mInstance;

    // The list of connections, for ease of internal access
    private List<Chat> mChats = new ArrayList<>();

    // The mutable live data to store the connections and update observers
    private MutableLiveData<List<Chat>> chats;

    /**
     * Private constructor to force use of factory method for access.
     */
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
     * Removes a new chat to the list of chats.
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
     */
    public void setUnread(final int chatID) {
        for(Chat c : mChats) {
            if(c.getChatID() == chatID)
                c.setNew(true);
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
