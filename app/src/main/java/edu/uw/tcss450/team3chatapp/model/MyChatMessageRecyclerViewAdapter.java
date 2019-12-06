package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.ui.ChatMessageFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ChatMessage} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** Identifies if the message was sent by the current user. */
    private static final int MY_MESSAGE = 1;
    /** Identifies if the message was not sent by the current user. */
    private static final int OTHER_MESSAGE = 0;

    /** The list of chat messages. */
    private final List<ChatMessage> mValues;
    /** The Listener for responding to interaction with RecyclerView items. */
    private final OnListFragmentInteractionListener mListener;

    public MyChatMessageRecyclerViewAdapter(List<ChatMessage> items,
                                            OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_mychatmessage, parent, false);
            return new MyViewHolder(view);
        }
        else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_chatmessage, parent, false);
            return new OtherViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(mValues.get(position).amSender())
            return MY_MESSAGE;
        else return OTHER_MESSAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == MY_MESSAGE) {
            ((MyViewHolder) holder).setInfo(mValues.get(position));
        } else {
            ((OtherViewHolder) holder).setInfo(mValues.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /** Class for a ViewHolder containing a chat message sent by the logged in user. */
    public class MyViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mSenderView;
        final TextView mTimestampView;
        final TextView mMessageView;
        ChatMessage mItem;

        MyViewHolder(View view) {
            super(view);
            mView = view;
            mSenderView = view.findViewById(R.id.tv_mychat_sender);
            mTimestampView = view.findViewById(R.id.tv_mychat_timestamp);
            mMessageView = view.findViewById(R.id.tv_mychat_message);
        }

        void setInfo(ChatMessage item) {
            mItem = item;
            mSenderView.setText(item.getSender());
            mTimestampView.setText(item.getTimestamp());
            mMessageView.setText(item.getMessage());

            mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mItem);
                }
            });
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mTimestampView.getText() + "'";
        }
    }

    /** Class for a ViewHolder containing a chat message sent by another user. */
    public class OtherViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mSenderView;
        final TextView mTimestampView;
        final TextView mMessageView;
        ChatMessage mItem;

        OtherViewHolder(View view) {
            super(view);
            mView = view;
            mSenderView = view.findViewById(R.id.tv_chat_sender);
            mTimestampView = view.findViewById(R.id.tv_chat_timestamp);
            mMessageView = view.findViewById(R.id.tv_chat_message);
        }

        void setInfo(ChatMessage item) {
            mItem = item;
            mSenderView.setText(item.getSender());
            mTimestampView.setText(item.getTimestamp());
            mMessageView.setText(item.getMessage());

            mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(mItem);
                }
            });
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mTimestampView.getText() + "'";
        }
    }
}
