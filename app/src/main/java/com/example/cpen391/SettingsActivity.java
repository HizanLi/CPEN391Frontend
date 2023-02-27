package com.example.cpen391;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                String ss = sharedPreferences.getAll().toString();
                Toast.makeText(SettingsActivity.this, "aaaaaaa", Toast.LENGTH_SHORT).show();
                Log.d("SETTING: ", ss);
                String upper = sharedPreferences.getString("upperTempLimit", "");
                Log.d("upper: ", upper);
                String lower = sharedPreferences.getString("lowerTempLimit", "");
                Log.d("lower: ", lower);
            }
        });
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final String TAG = "SettingsFragment";
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            final SharedPreferences.Editor editor = sharedPref.edit();

            EditTextPreference upperLim = findPreference("upperTempLimit");

            assert upperLim != null;
            upperLim.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    editText.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
                }
            });

            EditTextPreference lowerLim = findPreference("lowerTempLimit");
            assert lowerLim != null;
            lowerLim.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    editText.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
                }
            });
        }
    }
}