package com.example.cpen391;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class HomeActivity extends AppCompatActivity {

    public final String VM_public_ip = "http://3.96.148.29:8000/";

    private int maxTemp, minTemp, roomTemp;

    private TextView userNameTV;
    private final String TAG = "HomeActivity";

    // button for logout
    private Button logoutBtn;
    private TextView ctemperature, chumidity, tips, target_temperature;
    private Button power, submit, reset, history;
    CircularSeekBar seekbar;

    private int desire;

    private int upperInt, lowerInt;
    private final int thresholdTemp = 30;
    private final int redWarm = 255, greenWarm = 265, blueWarm = 265;
    private final int redCold = 153, greenCold = 190, blueCold = 255;

    private ImageView setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        update();

        minTemp = 0;
        maxTemp = 100;

//        Setting
        try{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            upperInt = Integer.parseInt(sharedPref.getString("upperTempLimit", null));
            lowerInt = Integer.parseInt(sharedPref.getString("lowerTempLimit", null));
            Log.d("upper: ", String.valueOf(upperInt));
            Log.d("lower: ", String.valueOf(lowerInt));
        }catch (Exception e){
            upperInt = 100;
            lowerInt = 0;
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
                if(lowerInt > upperInt){
                    Toast.makeText(HomeActivity.this, "lowerInt > upperInt, Please check you setting page", Toast.LENGTH_SHORT).show();
                }else{
//                    Log.d(TAG, "Progress: " + seekbar.getProgress());
                    float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                    int intTemp = (int)currentTemp;
                    if(lowerInt <= intTemp && intTemp <= upperInt){
                        target_temperature.setText(intTemp + "C");
                        if(intTemp > thresholdTemp){
                            back.setBackgroundColor(Color.rgb(redWarm, greenWarm - 2 * intTemp, blueWarm - 2 * intTemp));
                        }else {
                            back.setBackgroundColor(Color.rgb(redCold + intTemp, greenCold + intTemp, blueCold ));
                        }
                    }else{
                        Toast.makeText(HomeActivity.this, "Not in desired range", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {
                String desired_temp = target_temperature.getText().toString();
                Log.d(TAG, "desired_temp: " + desired_temp);
                //TODO: Store this target somewhere
            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar){
                //TODO: Store desired_temp before for reset purpose
            }
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
                String status = power.getText().toString();
                HashMap<String, String> data = new HashMap<>();
                //TODO: check connection with sever
                data.put("username", "SHA256_username");
                data.put("deviceId", "deviceId");
                if(status.equalsIgnoreCase("Power ON")){
                    power.setText("Power OFF");
                    data.put("status", "OFF");
                }else{
                    power.setText("Power ON");
                    data.put("status", "ON");
                }

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
                HashMap<String, String> data = new HashMap<>();
                //TODO: check connection with sever
                data.put("username", "SHA256_username");
                data.put("deviceId", "deviceId");
                data.put("desiredTemp", "123");
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                queue.start();

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "submit", new JSONObject(data),
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
    private void update(){
        HashMap<String, String> data = new HashMap<>();
        //TODO: check connection with sever
        //TODO: reset seekbar according to desired temperature

        data.put("username", "SHA256_username");
        data.put("deviceId", "deviceId");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "current", new JSONObject(data),
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
                            desire = response.getInt("desire_temp");
                            //on or off
                            int status = response.getInt("status");
                            chumidity.setText("" + chum);
                            ctemperature.setText("" + ctemp);
                            target_temperature.setText("" + desire);
                            if(desire == 1){
                                power.setText("Power ON");
                            }else{
                                power.setText("Power OFF");
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
        try{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            upperInt = Integer.parseInt(sharedPref.getString("upperTempLimit", ""));
            Log.d("upper: ", String.valueOf(upperInt));
            lowerInt = Integer.parseInt(sharedPref.getString("lowerTempLimit", ""));
            Log.d("lower: ", String.valueOf(lowerInt));
        }catch (Exception e){
            //first time open
            upperInt = 100;
            lowerInt = 0;
        }

    }

}
