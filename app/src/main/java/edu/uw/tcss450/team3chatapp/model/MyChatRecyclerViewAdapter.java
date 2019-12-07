package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.ui.ChatFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Chat} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** The list of Chats. */
    private final List<Chat> mValues;
    /** The Listener for normal interactions with RecyclerView items. */
    private final OnListFragmentInteractionListener mListener;
    /** The Listener for long click interactions with RecyclerView items */
    private final OnListFragmentInteractionListener mLongListener;
    /** Identifies if the Chat contains unread messages. */
    private final int UNREAD = 1;


    /**
     * Constructs a new MyChatRecyclerViewAdapter
     * @param items the Chats to populate the RecyclerView
     * @param listener the Listener for normal clicks
     * @param longListener the Listener for long clicks
     */
    public MyChatRecyclerViewAdapter(List<Chat> items, OnListFragmentInteractionListener listener,
                                     OnListFragmentInteractionListener longListener) {
        mValues = items;
        mListener = listener;
        mLongListener = longListener;
    }

    @Override
    public int getItemViewType(int position) {
        if(mValues.get(position).hasNew())
            return UNREAD;
        else return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == UNREAD) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_chat_unread, parent, false);
            return new UnreadViewHolder(view);
        }
        else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_chat, parent, false);
            return new ReadViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == UNREAD) {
            ((UnreadViewHolder) holder).setInfo(mValues.get(position));
        } else {
            ((ReadViewHolder) holder).setInfo(mValues.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /** Class for a ViewHolder containing a chat with unread messages. */
    public class UnreadViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mChatName;
        final TextView mChatDescription;
        Chat mItem;

        UnreadViewHolder(View view) {
            super(view);
            mView = view;
            mChatName = view.findViewById(R.id.tv_chatlist_unread_name);
            mChatDescription = view.findViewById(R.id.tv_chatlist_unread_description);
        }

        void setInfo(final Chat tChat) {
            mItem = tChat;
            mChatName.setText(tChat.getName());
            mChatDescription.setText(tChat.getDescription());

            mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mItem);
                }
            });

            mView.setOnLongClickListener(v -> {
                if (null != mLongListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mLongListener.onListFragmentInteraction(mItem);
                }
                return true;
            });
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mChatName.getText() + "'";
        }
    }

    /** Class for a ViewHolder containing a chat with no unread messages. */
    public class ReadViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mChatName;
        final TextView mChatDescription;
        Chat mItem;

        ReadViewHolder(View view) {
            super(view);
            mView = view;
            mChatName = view.findViewById(R.id.tv_chatlist_name);
            mChatDescription = view.findViewById(R.id.tv_chatlist_description);
        }

        void setInfo(final Chat tChat) {
            mItem = tChat;
            mChatName.setText(tChat.getName());
            mChatDescription.setText(tChat.getDescription());

            mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mItem);
                }
            });

            mView.setOnLongClickListener(v -> {
                if (null != mLongListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mLongListener.onListFragmentInteraction(mItem);
                }
                return true;
            });
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mChatName.getText() + "'";
        }
    }
}
