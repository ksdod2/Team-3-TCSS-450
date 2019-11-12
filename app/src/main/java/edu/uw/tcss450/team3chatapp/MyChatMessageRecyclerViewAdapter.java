package edu.uw.tcss450.team3chatapp;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.team3chatapp.ui.ChatMessageFragment.OnListFragmentInteractionListener;
import edu.uw.tcss450.team3chatapp.model.ChatMessage;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ChatMessage} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyChatMessageRecyclerViewAdapter extends RecyclerView.Adapter<MyChatMessageRecyclerViewAdapter.ViewHolder> {

    private final List<ChatMessage> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyChatMessageRecyclerViewAdapter(List<ChatMessage> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chatmessage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mSenderView.setText(mValues.get(position).getSender());
        holder.mTimestampView.setText(mValues.get(position).getTimestamp());
        holder.mMessageView.setText(mValues.get(position).getMessage());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mSenderView;
        public final TextView mTimestampView;
        public final TextView mMessageView;
        public ChatMessage mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSenderView = view.findViewById(R.id.tv_chat_sender);
            mTimestampView = view.findViewById(R.id.tv_chat_timestamp);
            mMessageView = view.findViewById(R.id.tv_chat_message);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTimestampView.getText() + "'";
        }
    }
}
