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
public class MyChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Chat> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final OnListFragmentInteractionListener mLongListener;
    private final int UNREAD = 1;
    private final int NO_UNREAD = 0;


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
        else return NO_UNREAD;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
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

    public class UnreadViewHolder extends RecyclerView.ViewHolder {
        protected final View mView;
        public final TextView mChatName;
        public final TextView mChatDescription;
        public Chat mItem;

        public UnreadViewHolder(View view) {
            super(view);
            mView = view;
            mChatName = view.findViewById(R.id.tv_chatlist_unread_name);
            mChatDescription = view.findViewById(R.id.tv_chatlist_unread_description);
        }

        public void setInfo(final Chat tChat) {
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

        @Override
        public String toString() {
            return super.toString() + " '" + mChatName.getText() + "'";
        }
    }

    public class ReadViewHolder extends RecyclerView.ViewHolder {
        protected final View mView;
        public final TextView mChatName;
        public final TextView mChatDescription;
        public Chat mItem;

        public ReadViewHolder(View view) {
            super(view);
            mView = view;
            mChatName = view.findViewById(R.id.tv_chatlist_name);
            mChatDescription = view.findViewById(R.id.tv_chatlist_description);
        }

        public void setInfo(final Chat tChat) {
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

        @Override
        public String toString() {
            return super.toString() + " '" + mChatName.getText() + "'";
        }
    }
}
