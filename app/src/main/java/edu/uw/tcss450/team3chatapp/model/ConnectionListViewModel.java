package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * A ViewModel for a list of connections associated with a user's account.
 * @author Kameron Dodd
 * @version 11/18/19
 */
public class ConnectionListViewModel extends ViewModel {

    // Using factory design pattern to ensure only one instance across application
    private static ConnectionListViewModel mInstance;

    // The list of connections, for ease of internal access
    private ArrayList<Connection> mConnections;

    // The mutable live data to store the connections and update observers
    private MutableLiveData<List<Connection>> connections;

    /**
     * Private constructor to force use of factory method for access.
     */
    private ConnectionListViewModel()  { connections = new MutableLiveData<>(); }

    /**
     * Method to get access to the live data to add observers.
     * @return the LiveData object containing the connections
     */
    public LiveData<List<Connection>> getCurrentConnections() { return connections; }

    /**
     * Adds a new connection or updated connection state to the list of connections.
     * @param tConnection the connection to add
     */
    public void addConnection(final Connection tConnection) {
        // Remove the old instance of the connection so the new one may replace it
        mConnections.remove(tConnection);
        // Add the new connection
        mConnections.add(tConnection);
        // Update the LiveData to alert to observers
        connections.setValue(mConnections);
    }

    /**
     * Removes a connection from the list of connections.
     * @param tConnection the connection to remove
     */
    public void removeConnection(final Connection tConnection) {
        // Remove the old instance of the connection
        mConnections.remove(tConnection);
        // Update the LiveData to alert observers
        connections.setValue(mConnections);
    }

    /**
     * Replaces the list of connections with a new one.
     * @param tConnections the list of connections to use
     */
    public void setConnections(final ArrayList<Connection> tConnections) {
        mConnections = tConnections;
        connections.setValue(mConnections);
    }

    /**
     * Factory method to provide the factory that gives access to the singleton instance.
     * @return the factory that provides the instance of this ViewModel
     */
    public static ViewModelProvider.Factory getFactory() {
        return new ViewModelProvider.Factory() {

            @NonNull
            @Override
            public ConnectionListViewModel create(@NonNull Class modelClass) {
                if (mInstance == null)
                    mInstance = new ConnectionListViewModel();
                return mInstance;
            }
        };
    }
}
