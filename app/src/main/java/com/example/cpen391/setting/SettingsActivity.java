package com.example.cpen391.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceFragmentCompat;

import com.example.cpen391.R;
import com.example.cpen391.chat.ChatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = "SettingsActivity";

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private static int backgroundColor = 0;
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

        Bundle extras = getIntent().getExtras();

        backgroundColor = extras.getInt("backgroundColor");

    }

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
                            if(!id.equalsIgnoreCase("Failed")){
                                deviceID.setSummary("Your device ID is: "+ id);
                                deviceID.setText(id);
                                Log.d(TAG, "id: "+ id);
                                writeToJson(getContext(), id);
                            }
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
                        }else if(s.equalsIgnoreCase("typedDeviceID")){
                            String id = sharedPreferences.getString(s, "");
                            preference.setSummary("Your Device ID is: " + id);
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
                    editText.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "60")});
                }
            });
            upperLim.setSummary("Current Setting: " + upperLim.getText() + "C");

            EditTextPreference lowerLim = findPreference("lowerTempLimit");
            assert lowerLim != null;
            lowerLim.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    editText.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "60")});
                }
            });
            lowerLim.setSummary("Current Setting: " + lowerLim.getText() + "C");

            EditTextPreference deviceIDD = findPreference("typedDeviceID");
            assert deviceIDD != null;

            deviceIDD.setSummary("Your Device ID is: "+deviceIDD.getText() );

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
            Preference sup = findPreference("chatbot");
            assert sup != null;
            sup.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(), ChatActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
            ListPreference dataPref = (ListPreference) findPreference("refresh_interval");
            assert dataPref != null;
            if(dataPref.getValue() == null){
                dataPref.setValueIndex(0); //set to index of your deafult value
            }

            Preference deleteALL = findPreference("deleteAllId");
            assert deleteALL != null;
            deleteALL.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    deviceIDD.setSummary("Your Device ID is: None" );
                    deleteCached(getContext());
                    return true;
                }
            });

        }

        private void deleteCached(Context applicationContext) {
            File myObj = new File(applicationContext.getFilesDir(), "allDevice.json");
            if (myObj.delete()) {
                System.out.println("Deleted the file: " + myObj.getName());
            } else {
                System.out.println("Failed to delete the file.");
            }
        }

        private void writeToJson(Context applicationContext, String DeviceID){
            Log.d(TAG, DeviceID);
            if(DeviceID.equalsIgnoreCase("Failed")){
                return;
            }

            File file = new File(String.valueOf(applicationContext.getFilesDir()), "allDevice.json");
            if(file.exists() && !file.isDirectory()) {
                try{
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line = bufferedReader.readLine();

                    while (line != null) {
                        stringBuilder.append(line);
                        line = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    // This response will have Json Format String
                    String result = stringBuilder.toString();
                    Log.d(TAG, "reading result: " + result);

                    JSONObject ids = new JSONObject(result);
                    JSONArray array = ids.getJSONArray("idsArray");

                    if(!hasValue(array,DeviceID)){
                        Log.d(TAG, "adding new device");
                        array.put(DeviceID);
                    }

                    try {
                        FileOutputStream writer = new FileOutputStream(file);
                        writer.write(ids.toString().getBytes());
                        writer.flush();
                        writer.close();
                        Log.d(TAG, "write to file path is: " + file.getCanonicalPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ;
                    }

                }catch (IOException e){
                    // Can not read file
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                JSONObject ids = new JSONObject();
                JSONArray array = new JSONArray();
                array.put("None");
                array.put(DeviceID);

                try {
                    ids.put("idsArray", array);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException");
                    e.printStackTrace();
                }

                try {
                    FileOutputStream writer = new FileOutputStream(file);
                    writer.write(ids.toString().getBytes());
                    writer.flush();
                    writer.close();
                    Log.d(TAG, "write to file path is: " + file.getCanonicalPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public boolean hasValue(JSONArray array, String value) throws JSONException {
            for(int i = 0; i < array.length(); i++) {
                if(array.get(i).toString().equalsIgnoreCase(value)) return true;
            }
            return false;
        }
    }
}