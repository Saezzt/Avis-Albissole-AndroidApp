package it.saezzt.avisalbissole;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;


public class AvisSettingsFragment extends PreferenceFragmentCompat {
    private final String TAG = "AvisSettingsFragment";
    private final String TO = "albissola.comunale@avis.it";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        SharedPreferences sharedPref = this.getActivity().getSharedPreferences(getString(R.string.SP_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        findPreference(getString(R.string.feedback)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Log.i("Send email", "");

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setData(Uri.parse("mailto:")); // per limitare a solo le app per le email
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.Feedback_subject));

                try {
                    startActivity(emailIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), "Impossibile inviare un Email", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, ex.getMessage());
                }
                return true;
            }
        });

        findPreference(getString(R.string.AlarmSetting)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String s = Boolean.toString(sharedPref.getBoolean(getString(R.string.AlarmSetting),false));
                Toast.makeText(getContext(), getString(R.string.AlarmSetting_title)+": "+s+" -> " + newValue, Toast.LENGTH_SHORT).show();
                editor.putBoolean(getString(R.string.AlarmSetting),(Boolean) newValue);
                editor.apply();
                return true;
            }
        });

        findPreference(getString(R.string.CalendarAlarmSet)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String s = Boolean.toString(sharedPref.getBoolean(getString(R.string.CalendarAlarmSet),true));
                Toast.makeText(getContext(), getString(R.string.CalendarAlarmSet_title)+": "+s+" -> " + newValue, Toast.LENGTH_SHORT).show();
                editor.putBoolean(getString(R.string.CalendarAlarmSet),(Boolean) newValue);
                editor.apply();
                return true;
            }
        });
    }
}
