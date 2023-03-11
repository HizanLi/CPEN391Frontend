package com.example.cpen391;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cpen391.R.id;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class HomeActivity extends AppCompatActivity {

//    public final String VM_public_ip = getString(R.string.ipAddress);

    public final String VM_public_ip = "http://15.222.248.41:8000/";

    private int maxTemp, minTemp, roomTemp;

    private TextView userNameTV;
    private final String TAG = "HomeActivity";

    // button for logout
    private Button logoutBtn;
    private TextView ctemperature, chumidity, tips, target_temperature;
    private Button power, submit, reset, history;
    CircularSeekBar seekbar;

    private int upperInt, lowerInt;
    private final int thresholdTemp = 30;
    private final int redWarm = 255, greenWarm = 265, blueWarm = 265;
    private final int redCold = 153, greenCold = 190, blueCold = 255;
    private String username, sha256username, deviceID, desiredTemp, status;
    private ImageView setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Bundle extras = getIntent().getExtras();

        if(extras == null) {
            username = null;
            sha256username = null;
        } else {
            username= extras.getString("username");
            sha256username= extras.getString("sha256username");
        }

        update();

        minTemp = 0;
        maxTemp = 100;

//        Setting
        try{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            upperInt = Integer.parseInt(sharedPref.getString("upperTempLimit", null));
            lowerInt = Integer.parseInt(sharedPref.getString("lowerTempLimit", null));
            deviceID = sharedPref.getString("typedDeviceID", null);

            Log.d("deviceID: ", deviceID);
            Log.d("upper: ", String.valueOf(upperInt));
            Log.d("lower: ", String.valueOf(lowerInt));

        }catch (Exception e){
            upperInt = 100;
            lowerInt = 0;

            username = null;
            sha256username = null;
            deviceID = null;
            desiredTemp = null;
            status = null;

            Log.d(TAG, "First time login to this app");
        }

        //Textview
        target_temperature = findViewById(R.id.target_temperature);
        ctemperature = findViewById(R.id.ctemperature);
        chumidity = findViewById(R.id.chumidity);
        tips = findViewById(R.id.tips);

        //Buttons
        power = findViewById(id.power);
        submit = findViewById(R.id.submit);
        reset = findViewById(R.id.reset);
        history = findViewById(R.id.viewHistory);

        //ImageView
        setting = findViewById(id.setting);

        ConstraintLayout back = findViewById(id.colorTheme);

        seekbar = findViewById(R.id.seek_bar);
        seekbar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@Nullable CircularSeekBar circularSeekBar, float v, boolean b){
                if(lowerInt < upperInt){
                    float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                    int intTemp = (int)currentTemp;

                    desiredTemp = Integer.toString(intTemp);

                    if(lowerInt <= intTemp && intTemp <= upperInt){
                        target_temperature.setText(intTemp + "C");
                        if(intTemp > thresholdTemp){
                            back.setBackgroundColor(Color.rgb(redWarm, greenWarm - 2 * intTemp, blueWarm - 2 * intTemp));
                        }else {
                            back.setBackgroundColor(Color.rgb(redCold + intTemp, greenCold + intTemp, blueCold ));
                        }
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {
                if( lowerInt > upperInt){
                    alert("Alert", "lowerInt > upperInt, jump to setting?");
                }else{
                    float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                    int intTemp = (int)currentTemp;
                    if(! (lowerInt <= intTemp && intTemp <= upperInt)){
                        Toast.makeText(HomeActivity.this, "Not in desired range", Toast.LENGTH_SHORT).show();
                        alert("Jump", "lowerInt > upperInt, jump to setting?");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar){}
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this,"View History", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(i);
            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String statusLo = power.getText().toString();
                HashMap<String, String> data = new HashMap<>();

                if(statusLo.equalsIgnoreCase("Power ON")){
                    power.setText("Power OFF");
                    status = "OFF";
                }else{
                    power.setText("Power ON");
                    status = "ON";
                }

                data.put("username", sha256username);
                data.put("device_id", deviceID);
                data.put("desire_temp", desiredTemp);
                data.put("status", status);

                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.start();

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "power", new JSONObject(data),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, "response.toString()");
                                Log.d(TAG, response.toString());
                                Toast.makeText(HomeActivity.this,"Server Received", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(HomeActivity.this,"Network Error", Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "onErrorResponse " + error.getMessage());
                            }
                        });
                queue.add(request);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
//                HashMap<String, String> data = new HashMap<>();
//
//                data.put("username", sha256username);
//                data.put("device_id", deviceID);
//                data.put("desire_temp", desiredTemp);
//                data.put("status", status);
//
//                Log.d(TAG, "In submit");
//                Log.d(TAG, data.toString());
//
//                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//                queue.start();
//
//                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "submit", new JSONObject(data),
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                Log.d(TAG, "response.toString()");
//                                Log.d(TAG, response.toString());
//                                Toast.makeText(HomeActivity.this,"Server Received", Toast.LENGTH_SHORT).show();
//                            }
//                        },
//                        new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                Toast.makeText(HomeActivity.this,"Network Error", Toast.LENGTH_SHORT).show();
//                                Log.d(TAG, "onErrorResponse " + error.getMessage());
//                            }
//                        });
//                queue.add(request);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                update();
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this,"Update Settings", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });
    }

    private void alert(String title, String content){
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.show();
    }
    private void update(){
        HashMap<String, String> data = new HashMap<>();
        //TODO: check connection with sever
        //TODO: reset seekbar according to desired temperature

        data.put("username", sha256username);
        data.put("device_id", deviceID);
        data.put("desire_temp", desiredTemp);
        data.put("status", status);

        Log.d(TAG, "In update");
        Log.d(TAG, data.toString());

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "recent", new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response.toString()");
                        Log.d(TAG, response.toString());
                        try {//TODO: check connection with sever
                            //current
                            int ctemp = response.getInt("current_temp");
                            int chum = response.getInt("current_hum");
                            //desired
                            desiredTemp = Integer.toString(response.getInt("desire_temp"));
                            //on or off
                            int ONOFF = response.getInt("status");
                            chumidity.setText(Integer.toString(chum) + "%");
                            ctemperature.setText(Integer.toString(ctemp) + "C");
                            target_temperature.setText(desiredTemp + "C");

                            if(ONOFF == 1){
                                power.setText("Power ON");
                                status = "ON";
                            }else{
                                power.setText("Power OFF");
                                status = "OFF";
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeActivity.this,"Network Error", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse " + error.getMessage());
                    }
                });
        queue.add(request);
    }

    @Override
    public void onResume(){
        super.onResume();
        update();
        Log.d(TAG, "onResume Called");
        try{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            upperInt = Integer.parseInt(sharedPref.getString("upperTempLimit", null));
            lowerInt = Integer.parseInt(sharedPref.getString("lowerTempLimit", null));
            deviceID = sharedPref.getString("typedDeviceID", null);

            Log.d("deviceID: ", deviceID);
            Log.d("upper: ", String.valueOf(upperInt));
            Log.d("lower: ", String.valueOf(lowerInt));

        }catch (Exception e){
            //first time open
            upperInt = 100;
            lowerInt = 0;

            username = null;
            sha256username = null;
            deviceID = null;
            desiredTemp = null;
            status = null;

            Log.d(TAG, "First time login to this app");
        }

    }
}
