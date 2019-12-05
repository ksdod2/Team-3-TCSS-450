package edu.uw.tcss450.team3chatapp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uw.tcss450.team3chatapp.model.WeatherProfile;
import edu.uw.tcss450.team3chatapp.ui.WeatherProfileFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link WeatherProfile} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyWeatherProfileRecyclerViewAdapter extends RecyclerView.Adapter<MyWeatherProfileRecyclerViewAdapter.ViewHolder> {

    private final List<WeatherProfile> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyWeatherProfileRecyclerViewAdapter(List<WeatherProfile> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_weatherprofile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getCityState());
        holder.mContentView.setText(mValues.get(position).getCurrentWeather());

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mIdView;
        final TextView mContentView;
        WeatherProfile mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.tv_weatherprof_location);
            mContentView = view.findViewById(R.id.tv_weatherprof_temp);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
