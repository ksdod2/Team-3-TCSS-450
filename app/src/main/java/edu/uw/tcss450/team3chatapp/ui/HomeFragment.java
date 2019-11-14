package edu.uw.tcss450.team3chatapp.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.uw.tcss450.team3chatapp.HomeActivityArgs;
import edu.uw.tcss450.team3chatapp.R;

public class HomeFragment extends Fragment {

    private TextView weatherTemp;
    private TextView weatherDecrip;

    class Weather extends AsyncTask<String,Void,String> {



        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* Showing the ProgressBar, Making the main design GONE */


        }

        @Override
        protected String doInBackground(String... strings) {

            String response = "";
            HttpURLConnection urlConnection = null;
            try {
                URL urlObject = new URL("http://openweathermap.org/data/2.5/weather?q=Tacoma&appid=b6907d289e10d714a6e88b30761fae22&units=imperial");
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while ((s = buffer.readLine()) != null) {
                    response += s;
                }
                Log.i("HELLO", response);

            } catch (Exception e) {
                response = "Unable to connect, Reason: "
                        + e.getMessage();
                Log.i("BROKEN", "BROKEN");
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                String temp = main.getString("temp");
                Log.i("TEMP = ", temp);
                String weatherDescription = weather.getString("description");

                weatherTemp.setText(temp);
                weatherDecrip.setText(weatherDescription);


            } catch (JSONException e) {
                Log.e("WRONG", "BAD API");
            }
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //******NEW********

        new Weather().execute();
        //******NEW*********

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeActivityArgs args = HomeActivityArgs.fromBundle(getArguments());
        TextView greeting = getView().findViewById(R.id.tv_home_greeting);

        weatherDecrip = getView().findViewById(R.id.tv_home_status);
        weatherTemp = getView().findViewById(R.id.tv_home_temperature);
        greeting.setText("Welcome, " + args.getCredentials().getFirstName() + " " + args.getCredentials().getLastName() + "!");

//            weatherAPI = weather.execute("http://openweathermap.org/data/2.5/weather?q=Tacoma&appid=b6907d289e10d714a6e88b30761fae22&units=imperial").get();

    }

}