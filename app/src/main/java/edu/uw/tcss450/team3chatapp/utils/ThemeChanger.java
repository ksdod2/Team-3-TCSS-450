package edu.uw.tcss450.team3chatapp.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Objects;

import edu.uw.tcss450.team3chatapp.HomeActivityArgs;

/**
 * Provides static helper methods needed for changing the app theme from the settings menu.
 */
public class ThemeChanger {

    /**
     * Given the current activity, rebundles the arguments and starts a
     * new instance it in order to apply the theme change.
     *
     * @param theActivity   current activity to restart
     */
    public static void applyChange(Activity theActivity) {
        Bundle args = HomeActivityArgs.fromBundle(Objects.requireNonNull(theActivity.getIntent().getExtras())).toBundle();
        Intent intent = new Intent(theActivity, theActivity.getClass());
        intent.putExtras(args);

        theActivity.finish();
        theActivity.startActivity(intent);
    }

    /**
     * Applies given theme to given activity context.
     *
     * @param theActivity   context activity to apply the theme to.
     * @param theThemeId    id of theme to apply.
     */
    public static void setThemeOnActivityCreation(Activity theActivity, int theThemeId) {
        theActivity.setTheme(theThemeId);

        Log.d("THEME", theActivity.getResources().getResourceName(theThemeId));
    }
}