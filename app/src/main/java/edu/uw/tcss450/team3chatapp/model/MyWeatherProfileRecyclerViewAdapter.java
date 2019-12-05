package edu.uw.tcss450.team3chatapp.model;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.team3chatapp.R;
import edu.uw.tcss450.team3chatapp.ui.WeatherProfileFragment.OnListFragmentInteractionListener;

import java.util.List;
import java.util.Objects;

/**
 * {@link RecyclerView.Adapter} that can display a {@link WeatherProfile} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyWeatherProfileRecyclerViewAdapter extends RecyclerView.Adapter<MyWeatherProfileRecyclerViewAdapter.ViewHolder> {

    private final List<WeatherProfile> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Context mContext;

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
        mContext = holder.mView.getContext();
        holder.mWP = mValues.get(position);

        try {
            JSONObject rootAll = new JSONObject(holder.mWP.get10DayForecast());
            JSONObject rootToday = getFirst(rootAll);

            // Get info from JSON
            String name = holder.mWP.getCityState();
            String icFile = rootToday.getString(mContext.getString(R.string.keys_json_weather_icon))
                    .substring(0, rootToday.getString(mContext.getString(R.string.keys_json_weather_icon)).length()-4);
            String desc = rootToday.getString(mContext.getString(R.string.keys_json_weather_primary)) + '\u00A0';
            int intRain = Integer.parseInt(rootToday.getString(mContext.getString(R.string.keys_json_weather_precipitation)));
            String pop = intRain + "%";
            String highTemp = "F".equals(holder.mUnits)
                    ? rootToday.getString(mContext.getString(R.string.keys_json_weather_maxtempf))
                    : rootToday.getString(mContext.getString(R.string.keys_json_weather_maxtempc));
            highTemp += mContext.getString(R.string.misc_temp_unit_symbol);
            String lowTemp = "F".equals(holder.mUnits)
                    ? rootToday.getString(mContext.getString(R.string.keys_json_weather_mintempf))
                    : rootToday.getString(mContext.getString(R.string.keys_json_weather_mintempc));
            lowTemp += mContext.getString(R.string.misc_temp_unit_symbol);

            // Display Info
            holder.mLocationName.setText(name);
            holder.mWeatherDesc.setText(desc);
            holder.mRainChance.setText(pop);
            holder.mHighTemp.setText(highTemp);
            holder.mHighTemp.setTypeface(Typeface.DEFAULT_BOLD);
            holder.mLowTemp.setText(lowTemp);
            holder.mIcon.setImageResource(mContext.getResources().getIdentifier(icFile, "mipmap", Objects.requireNonNull(mContext.getPackageName())));

            //Display context specific info
            if(intRain > 20) {holder.mRainChance.setVisibility(View.VISIBLE);}
            if(position == 0) {
                holder.mSpace.setVisibility(View.GONE);
                holder.mCurLocIcon.setVisibility(View.VISIBLE);
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mWP);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final Space mSpace;
        final ImageView mCurLocIcon;
        final TextView mLocationName;
        final TextView mWeatherDesc;
        final TextView mRainChance;
        final TextView mHighTemp;
        final TextView mLowTemp;
        final ImageView mIcon;
        final String mUnits;
        WeatherProfile mWP;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mSpace = view.findViewById(R.id.space);
            mCurLocIcon = view.findViewById(R.id.iv_weatherprof_curLocationIcon);
            mLocationName = view.findViewById(R.id.tv_weatherprof_location);
            mWeatherDesc = view.findViewById(R.id.tv_weatherprof_locDesc);
            mRainChance = view.findViewById(R.id.tv_weatherprof_pop);
            mHighTemp = view.findViewById(R.id.tv_weatherprf_highTemp);
            mLowTemp = view.findViewById(R.id.tv_weatherprf_LowTemp);
            mIcon = view.findViewById(R.id.iv_weatherprof_icon);

            SharedPreferences prefs = view.getContext().getSharedPreferences(view.getContext().getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
            if(prefs.contains(view.getContext().getString(R.string.keys_prefs_tempunit))) {
                mUnits = prefs.getString(view.getContext().getString(R.string.keys_prefs_tempunit), "F");
            } else { //Otherwise set units to default (imperial)
                mUnits = "F";
                prefs.edit().putString(view.getContext().getString(R.string.keys_prefs_tempunit), "F").apply();
            }
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mWeatherDesc.getText() + "'";
        }
    }

    private JSONObject getFirst(final JSONObject theListJSON) {
        JSONObject first = null;

        try {
            first = theListJSON
                    .getJSONArray(mContext.getString(R.string.keys_json_weather_response))
                    .getJSONObject(0)
                    .getJSONArray(mContext.getString(R.string.keys_json_weather_periods_array))
                    .getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return first;
    }
}
