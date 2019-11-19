package edu.uw.tcss450.team3chatapp;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.team3chatapp.ui.ChatFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.team3chatapp.model.Chat;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Chat} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyChatRecyclerViewAdapter extends RecyclerView.Adapter<MyChatRecyclerViewAdapter.ViewHolder> {

    private final List<Chat> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final OnListFragmentInteractionListener mLongListener;


    public MyChatRecyclerViewAdapter(List<Chat> items, OnListFragmentInteractionListener listener,
                                     OnListFragmentInteractionListener longListener) {
        mValues = items;
        mListener = listener;
        mLongListener = longListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mChatName.setText(mValues.get(position).getName());
        holder.mChatDescription.setText(mValues.get(position).getDescription());

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });

        holder.mView.setOnLongClickListener(v -> {
            if (null != mLongListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mLongListener.onListFragmentInteraction(holder.mItem);
            }
        return true;
        });


    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected final View mView;
        public final TextView mChatName;
        public final TextView mChatDescription;
        public Chat mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mChatName = view.findViewById(R.id.tv_chatlist_name);
            mChatDescription = view.findViewById(R.id.tv_chatlist_description);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mChatName.getText() + "'";
        }
    }
}
