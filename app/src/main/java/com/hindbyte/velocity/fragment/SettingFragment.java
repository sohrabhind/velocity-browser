package com.hindbyte.velocity.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.intent.ClearData;
import com.hindbyte.velocity.util.ToastWindow;

import java.util.Objects;

public class SettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ListPreference searchEngine;

    private String[] seEntries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_setting);
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        Objects.requireNonNull(sp).registerOnSharedPreferenceChangeListener(this);
        String summary;

        seEntries = getResources().getStringArray(R.array.entries_search_engine);
        searchEngine = findPreference(getString(R.string.sp_search_engine));
        int num = Integer.valueOf(Objects.requireNonNull(sp.getString(getString(R.string.sp_search_engine), "0")));
        if (0 <= num && num <= 4) {
            summary = seEntries[num];
            searchEngine.setSummary(summary);
        }
        Preference versionName = findPreference(getString(R.string.setting_title_version));
        try {
            Objects.requireNonNull(versionName).setSummary(requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Objects.requireNonNull(getPreferenceScreen().getSharedPreferences()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        switch (Objects.requireNonNull(preference.getTitle()).toString()) {
            case "Clear Data":
                new AlertDialog.Builder(requireActivity())
                        .setMessage(R.string.toast_clear)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                            ClearData clearData = new ClearData(getActivity());
                            clearData.clearCaches();
                            ToastWindow toastWindow = new ToastWindow();
                            toastWindow.makeText(getActivity(), "Data Cleared", 1000);
                        }).create().show();
                break;
            case "Rate and Update":
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + requireActivity().getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
                }
                break;
            case "Version":
                Toast.makeText(getActivity(), R.string.toast_emoji, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (key.equals(getString(R.string.sp_search_engine))) {
            int num = Integer.valueOf(Objects.requireNonNull(sp.getString(key, "0")));
            if (0 <= num && num <= 4) {
                searchEngine.setSummary(seEntries[num]);
            }
        }
    }
}