package edu.uw.tcss450.team3chatapp;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.team3chatapp.model.Connection;
import edu.uw.tcss450.team3chatapp.ui.ConnectionHomeFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Connection} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyConnectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int SEARCH_RESULT = 0;
    private static final int EXISTING_CONTACT = 1;

    private final List<Connection> mConnections;
    private final OnListFragmentInteractionListener mListener;

    public MyConnectionRecyclerViewAdapter(List<Connection> items, OnListFragmentInteractionListener listener) {
        mConnections = items;
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if(mConnections.get(position).getRelation() == Connection.Relation.NONE)
            return SEARCH_RESULT;
        else return EXISTING_CONTACT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if(viewType == EXISTING_CONTACT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_connection, parent, false);
            return new ExistingViewHolder(view);
        }
        else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_connection_search_result, parent, false);
            return new SearchViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == EXISTING_CONTACT) {
            ((ExistingViewHolder) holder).setInfo(mConnections.get(position));
        } else {
            ((SearchViewHolder) holder).setInfo(mConnections.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mConnections.size();
    }

    public class ExistingViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mUsername;
        public final TextView mEmail;
        public Connection mItem;

        public ExistingViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.tv_connection_list_name);
            mUsername = view.findViewById(R.id.tv_connection_list_username);
            mEmail = view.findViewById(R.id.tv_connection_list_email);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mUsername.getText() + "'";
        }

        public void setInfo(Connection item) {
            mItem = item;
            mNameView.setText(item.getFirstName() + " " + item.getLastName());
            mUsername.setText(item.getUsername());
            mEmail.setText(item.getEmail());

            mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mItem);
                }
            });
        }
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mUsername;
        public final TextView mEmail;
        public Connection mItem;

        public SearchViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.tv_connectionsearch_list_name);
            mUsername = view.findViewById(R.id.tv_connectionsearch_list_username);
            mEmail = view.findViewById(R.id.tv_connectionsearch_list_email);
        }


        @Override
        public String toString() {
            return super.toString() + " '" + mUsername.getText() + "'";
        }

        public void setInfo(Connection item) {
            mItem = item;
            mNameView.setText(item.getFirstName() + " " + item.getLastName());
            mUsername.setText(item.getUsername());
            mEmail.setText(item.getEmail());

            mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mItem);
                }
            });
        }

    }
}
