package com.example.cpen391;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = "SettingsActivity";

    public final String VM_public_ip = "http://3.96.148.29:8000/";

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
    }
    //Purpose: use camera to scan the QR code
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "AAAAAASDFGHJK");
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned : " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
        Log.d(TAG, "DCFVYGBUHNJK");

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final String TAG = "SettingsFragment";
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

            sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    Preference preference = findPreference(s);

                    Log.d("s is: ", s);
                    String ss = sharedPreferences.getAll().toString();
                    Log.d("SETTING: ", ss);

                    if(preference instanceof EditTextPreference){
                        if(s.equalsIgnoreCase("lowerTempLimit") ||s.equalsIgnoreCase("upperTempLimit")){
                            String temp = sharedPreferences.getString(s, "");
                            preference.setSummary("Current Setting: " + temp + "C");
                        }else if(s.equalsIgnoreCase("typeDeviceID")){
                            String id = sharedPreferences.getString(s, "");
                            preference.setSummary("Current Device ID: " + id);
                        }
                    }
                }
            });

            EditTextPreference upperLim = findPreference("upperTempLimit");

            assert upperLim != null;
            upperLim.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    editText.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
                }
            });
            upperLim.setSummary("Current Setting: " + upperLim.getText() + "C");

            EditTextPreference lowerLim = findPreference("lowerTempLimit");
            assert lowerLim != null;
            lowerLim.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    editText.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "100")});
                }
            });
            lowerLim.setSummary("Current Setting: " + lowerLim.getText() + "C");

            Preference button = findPreference("qrScanner");
            assert button != null;
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    IntentIntegrator integrator = IntentIntegrator.forSupportFragment(SettingsFragment.this);
                    integrator.setOrientationLocked(false);
                    integrator.setPrompt("Scan QR code");
                    integrator.setBeepEnabled(false);
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    integrator.initiateScan();
                    return true;
                }
            });
        }
    }
}