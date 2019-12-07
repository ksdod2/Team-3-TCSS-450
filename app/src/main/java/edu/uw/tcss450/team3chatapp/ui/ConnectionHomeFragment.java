package edu.uw.tcss450.team3chatapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team3chatapp.model.MyConnectionRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Connection;
import edu.uw.tcss450.team3chatapp.model.ConnectionListViewModel;

/**
 * A fragment to show all the current user's connections.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ConnectionHomeFragment extends Fragment {

    /** The current user's MemberID. */
    private int mMemberID;
    /** The current user's JWT. */
    private String mJWT;
    /** The List of the current user's verified connections. */
    private ArrayList<Connection> mCurrent = new ArrayList<>();
    /** The List of the current user's incoming connection requests. */
    private ArrayList<Connection> mIncoming = new ArrayList<>();
    /** The List of the current user's pending sent connection requests. */
    private ArrayList<Connection> mPending = new ArrayList<>();

    /** Required empty public constructor. */
    public ConnectionHomeFragment() {}

    /** {@inheritDoc} */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection_list, container, false);

        ConnectionHomeFragmentArgs args =
                ConnectionHomeFragmentArgs.fromBundle(Objects.requireNonNull(getArguments()));

        mMemberID = args.getMemberID();
        mJWT = args.getJWT();

        // Add this fragment as an observer to the connection ViewModel
        ConnectionListViewModel model =
                ConnectionListViewModel.getFactory().create(ConnectionListViewModel.class);
        model.getCurrentConnections().observe(this, this::updateRecyclerViews);

        // Build out initial RecyclerViews
        for(Connection connection : Objects.requireNonNull(model.getCurrentConnections().getValue())) {
            if(connection.getRelation() == Connection.Relation.ACCEPTED)
                mCurrent.add(connection);
            else if(connection.amSender())
                mPending.add(connection);
            else
                mIncoming.add(connection);
        }

        RecyclerView currentContacts = rootView.findViewById(R.id.list_connections_accepted);
        currentContacts.setAdapter(new MyConnectionRecyclerViewAdapter(mCurrent, this::displayConnection));

        RecyclerView incomingContacts = rootView.findViewById(R.id.list_connections_incoming);
        incomingContacts.setAdapter(new MyConnectionRecyclerViewAdapter(mIncoming, this::displayConnection));

        RecyclerView pendingContacts = rootView.findViewById(R.id.list_connections_pending);
        pendingContacts.setAdapter(new MyConnectionRecyclerViewAdapter(mPending, this::displayConnection));

        Button searchBtn = rootView.findViewById(R.id.btn_connections_search);
        searchBtn.setOnClickListener(v -> {
            ConnectionHomeFragmentDirections.ActionNavConnectionhomeToNavConnectionsearch searchFragment
                    = ConnectionHomeFragmentDirections.actionNavConnectionhomeToNavConnectionsearch(mMemberID, mJWT);
            Navigation.findNavController(v).navigate(searchFragment);
        });

        return rootView;
    }

    /**
     * Updates all three RecyclerViews upon change to connections.
     * @param tConns the list of Connections involving the user
     */
    private void updateRecyclerViews(final List<Connection> tConns) {
        View rootView = getView();
        // Fill out connections based on changes to connection LiveData
        mCurrent.clear();
        mPending.clear();
        mIncoming.clear();
        for(Connection connection : tConns) {
            if(connection.getRelation() == Connection.Relation.ACCEPTED)
                mCurrent.add(connection);
            else if(connection.amSender())
                mPending.add(connection);
            else
                mIncoming.add(connection);
        }

        RecyclerView currentContacts =
                Objects.requireNonNull(rootView).findViewById(R.id.list_connections_accepted);
        Objects.requireNonNull(currentContacts.getAdapter()).notifyDataSetChanged();

        RecyclerView incomingContacts = rootView.findViewById(R.id.list_connections_incoming);
        Objects.requireNonNull(incomingContacts.getAdapter()).notifyDataSetChanged();

        RecyclerView pendingContacts = rootView.findViewById(R.id.list_connections_pending);
        Objects.requireNonNull(pendingContacts.getAdapter()).notifyDataSetChanged();
    }

    /**
     * Moves to display a given connection.
     * @param tConn the Connection to display
     */
    private void displayConnection(final Connection tConn) {
        NavController nc = Navigation.findNavController(Objects.requireNonNull(getView()));
        if (Objects.requireNonNull(nc.getCurrentDestination())
                .getId() != R.id.nav_connectionhome) { // Ensure back button doesn't break nav
            nc.navigateUp();
        }
        // Pass connection as arg, allow fragment to determine layout on its own
        ConnectionHomeFragmentDirections.ActionNavConnectionhomeToNavConnectionview connectionView =
            ConnectionHomeFragmentDirections.actionNavConnectionhomeToNavConnectionview(tConn, mMemberID, mJWT);
        nc.navigate(connectionView);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Connection item);
    }
}
