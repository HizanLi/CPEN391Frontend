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
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
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

//        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
//            @Override
//            public void onActivityResult(ActivityResult result) {
//                Log.d(TAG, "I am vback");
//                IntentResult intentResult = IntentIntegrator.parseActivityResult(result.getResultCode(), result.getResultCode(), result.getData());
//                // if the intentResult is null then
//                // toast a message as "cancelled"
//                if (intentResult != null) {
//                    if (intentResult.getContents() == null) {
//                        Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        Log.d(TAG, "as");
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                // if the intentResult is not null we'll set
                // the content and format of scan message

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

            Preference button = findPreference("qrScanner");
            assert button != null;
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //code for what you want it to do
                    IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
                    intentIntegrator.setPrompt("Scan a barcode or QR Code");
                    intentIntegrator.setOrientationLocked(true);
                    intentIntegrator.initiateScan();
                    return true;
                }
            });

            EditTextPreference deviceID = findPreference("deviceID");
            assert deviceID != null;
        }
    }
}