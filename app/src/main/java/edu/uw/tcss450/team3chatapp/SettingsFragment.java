package edu.uw.tcss450.team3chatapp;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {


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
            case R.id.theme1:
                Log.d("SETTINGS_MENU", "THEME 1 PRESSED");
                return true;
            case R.id.theme2:
                Log.d("SETTINGS_MENU", "THEME 2 PRESSED");
                return true;
            case R.id.theme3:
                Log.d("SETTINGS_MENU", "THEME 3 PRESSED");
                return true;
            default:
                return false;
        }
    }
}