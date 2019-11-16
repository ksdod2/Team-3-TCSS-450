package edu.uw.tcss450.team3chatapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import edu.uw.tcss450.team3chatapp.utils.ThemeChanger;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {

    SharedPreferences mPrefs;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPrefs = getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);

        FrameLayout themeOption = getView().findViewById(R.id.frame_settings_theme);
        themeOption.setOnClickListener(this::showThemeChangePopup);
    }

    private void showThemeChangePopup(final View theView) {
        PopupMenu popup = new PopupMenu(getContext(), theView, Gravity.END);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.settings_theme, popup.getMenu());
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_themeoption_default:
                updateTheme(R.style.AppTheme);
                return true;
            case R.id.settings_themeoption_spookyorange:
                updateTheme(R.style.SpookyOrangeTheme);
                return true;
            case R.id.settings_themeoption_bashfulblue:
                updateTheme(R.style.BashfulBlueTheme);
                return true;
            case R.id.settings_themeoption_huskypride:
                updateTheme(R.style.HuskyPrideTheme);
                return true;
            default:
                return false;
        }
    }

    private void updateTheme(int theThemeId) {
        mPrefs.edit().putInt(getString(R.string.keys_prefs_theme), theThemeId).apply();
        ThemeChanger.applyChange(getActivity());
    }
}