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
import java.util.Arrays;

import edu.uw.tcss450.team3chatapp.MyConnectionRecyclerViewAdapter;
import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.model.Connection;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ConnectionHomeFragment extends Fragment {

    private int mMemberID;
    private String mJWT;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ConnectionHomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connection_list, container, false);

        ConnectionHomeFragmentArgs args = ConnectionHomeFragmentArgs.fromBundle(getArguments());

        mMemberID = args.getMemberID();
        mJWT = args.getJWT();

        // Build out RecyclerViews and their information from navigation arguments
        ArrayList<Connection> current = new ArrayList<>(Arrays.asList(args.getAcceptedContacts()));
        ArrayList<Connection> incoming = new ArrayList<>(Arrays.asList(args.getIncomingContacts()));
        ArrayList<Connection> pending = new ArrayList<>(Arrays.asList(args.getPendingContacts()));

        RecyclerView currentContacts = rootView.findViewById(R.id.list_connections_accepted);
        currentContacts.setAdapter(new MyConnectionRecyclerViewAdapter(current, this::displayConnection));

        RecyclerView incomingContacts = rootView.findViewById(R.id.list_connections_incoming);
        incomingContacts.setAdapter(new MyConnectionRecyclerViewAdapter(incoming, this::displayConnection));

        RecyclerView pendingContacts = rootView.findViewById(R.id.list_connections_pending);
        pendingContacts.setAdapter(new MyConnectionRecyclerViewAdapter(pending, this::displayConnection));

        Button searchBtn = rootView.findViewById(R.id.btn_connections_search);
        searchBtn.setOnClickListener(v -> {
            ConnectionHomeFragmentDirections.ActionNavConnectionhomeToNavConnectionsearch searchFragment
                    = ConnectionHomeFragmentDirections.actionNavConnectionhomeToNavConnectionsearch(mMemberID, mJWT);
            Navigation.findNavController(v).navigate(searchFragment);
        });

        return rootView;
    }

    private void displayConnection(final Connection tConn) {
        NavController nc = Navigation.findNavController(getView());
        if (nc.getCurrentDestination().getId() != R.id.nav_connectionhome) // Ensure back button doesn't break nav
            nc.navigateUp();
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
        // TODO: Update argument type and name
        void onListFragmentInteraction(Connection item);
    }
}
