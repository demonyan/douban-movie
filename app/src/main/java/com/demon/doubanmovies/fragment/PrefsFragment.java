package com.demon.doubanmovies.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.demon.doubanmovies.R;

import de.psdev.licensesdialog.LicensesDialog;

public class PrefsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    private static final String ABOUT = "about";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        findPreference(ABOUT).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(ABOUT)) {
            showApacheLicenseDialog();
        }
        return false;
    }

    private void showApacheLicenseDialog() {

        new LicensesDialog.
                Builder(getActivity()).
                setNotices(R.raw.notices).
                setIncludeOwnLicense(true).
                build().
                showAppCompat();
    }
}
