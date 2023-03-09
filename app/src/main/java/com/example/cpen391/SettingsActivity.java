package com.example.cpen391;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceFragmentCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SettingsActivity extends AppCompatActivity {
    private final String TAG = "SettingsActivity";
    private ActivityResultLauncher<Intent> activityResultLauncher;
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
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        // if the intentResult is null then
//        // toast a message as "cancelled"
//        Log.d(TAG, "a------------------------------------------------------------------s");
//        if (intentResult != null) {
//            if (intentResult.getContents() == null) {
//                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "unsuccess");
//            } else {
//                // if the intentResult is not null we'll set
//                // the content and format of scan message
//                Log.d(TAG, "success");
//                String id = intentResult.getContents();
//                Log.d(TAG, id);
//
//            }
//        } else {
//            super.onActivityResult(requestCode, resultCode, data);
//        }
//    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final String TAG = "SettingsFragment";
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference deviceID = findPreference("typedDeviceID");
            assert  deviceID != null;

            ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent intent = result.getData();
                            assert intent != null;
                            String id = intent.getStringExtra("MESSAGE");
                            deviceID.setSummary("Your device is: "+ id);
                            deviceID.setText(id);
                        }
                    });


            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

            final SharedPreferences.Editor editor = sharedPref.edit();

            sharedPref.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                    Preference preference = findPreference(s);

                    Log.d("changed preference is: ", s);
                    String allSettings = sharedPreferences.getAll().toString();
                    Log.d("ALL SETTING: ", allSettings);

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
                    Intent intent = new Intent(getContext(), QRcodeActivity.class);
                    someActivityResultLauncher.launch(intent);
                    return true;
                }
            });
        }
    }
}