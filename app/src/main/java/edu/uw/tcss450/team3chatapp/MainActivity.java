package edu.uw.tcss450.team3chatapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import edu.uw.tcss450.team3chatapp.utils.ThemeChanger;
import me.pushy.sdk.Pushy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushy.listen(this);

        //Apply user-preferred theme from shared preferences before setContentView
        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        if(prefs.contains(getString(R.string.keys_prefs_theme))) {
            int themeId = prefs.getInt(getString(R.string.keys_prefs_theme), R.style.AppTheme);
            ThemeChanger.setThemeOnActivityCreation(this, themeId);
        } else {
            prefs.edit().putInt(getString(R.string.keys_prefs_theme), R.style.AppTheme).apply();
            ThemeChanger.setThemeOnActivityCreation(this, R.style.AppTheme);
        }

        setContentView(R.layout.activity_main);
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("type")) {
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .setGraph(R.navigation.nav_graph_login, getIntent().getExtras());
            }
        }
    }
}