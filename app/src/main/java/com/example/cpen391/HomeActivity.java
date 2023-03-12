package com.example.cpen391;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cpen391.R.id;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class HomeActivity extends AppCompatActivity {

//    public final String VM_public_ip = getString(R.string.ipAddress);

    public static String VM_public_ip;

    private final int maxTemp = 100, minTemp = 0;

    private final String TAG = "HomeActivity";

    private TextView ctemperature, chumidity, target_temperature;
    private Button power, submit, reset, history;
    CircularSeekBar seekbar;

    private int upperInt, lowerInt;
    private final int thresholdTemp = 30;
    private final int redWarm = 255, greenWarm = 265, blueWarm = 265;
    private final int redCold = 153, greenCold = 190, blueCold = 255;
    private static String username, sha256username, deviceID, desiredTemp, status;
    private ImageView setting;

    private ConstraintLayout background;
    private LinearLayout reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        VM_public_ip = getString(R.string.ipAddress);

        Bundle extras = getIntent().getExtras();

        username= extras.getString("username");
        sha256username= extras.getString("sha256username");

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

            deviceID = "None";
            desiredTemp = "26";
            status = "ON";

            Log.d(TAG, "First time login to this app");
        }

        //Textview
        target_temperature = findViewById(R.id.target_temperature);
        ctemperature = findViewById(R.id.ctemperature);
        chumidity = findViewById(R.id.chumidity);

        //Buttons
        power = findViewById(id.power);
        submit = findViewById(R.id.submit);
        reset = findViewById(R.id.reset);
        history = findViewById(R.id.viewHistory);

        //ImageView
        setting = findViewById(id.setting);

        background = findViewById(id.colorTheme);

        reminder = findViewById(R.id.remainder_layout);

        seekbar = findViewById(R.id.seek_bar);

        seekbar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@Nullable CircularSeekBar circularSeekBar, float v, boolean b){
                if(lowerInt < upperInt){
                    float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                    int intTemp = (int)currentTemp;

                    desiredTemp = Integer.toString(intTemp);

                    updateTV(intTemp);
                }
            }

            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {
                //In settings, lowerInt > upperInt
                if( lowerInt > upperInt){
                    alert("Alert", "lowerInt > upperInt, jump to setting?");
                }else{

                    reminder.setVisibility(View.VISIBLE);

                    float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                    int intTemp = (int)currentTemp;
                    if(! (lowerInt <= intTemp && intTemp <= upperInt)){
                        Toast.makeText(HomeActivity.this, "Not in desired range", Toast.LENGTH_SHORT).show();
                        alert("Jump", "Not in desired Range, jump to setting?");
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
                Log.d(TAG, "deviceID: " + deviceID);
                i.putExtra("deviceID", deviceID);
                i.putExtra("sha256username", sha256username);

                startActivity(i);
            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reminder.setVisibility(View.VISIBLE);

                String statusLo = power.getText().toString();
                HashMap<String, String> data = new HashMap<>();

                if(statusLo.equalsIgnoreCase("Power ON")){
                    power.setText("Power OFF");
                    status = "OFF";
                }else{
                    power.setText("Power ON");
                    status = "ON";
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reminder.setVisibility(View.INVISIBLE);

                update(1);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reminder.setVisibility(View.INVISIBLE);

                update(2);
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
        updateTV(26);
        update(2);
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
    /*
        int mode: 1 is submit, 2 is reset
     */
    private void update(int mode){
        HashMap<String, String> data = new HashMap<>();

        data.put("username", sha256username);
        data.put("device_id", deviceID);
        data.put("desire_temp", desiredTemp);

        if(status == null){
            status = "ON";
        }
        data.put("status", status);


        Log.d(TAG, "In update");
        Log.d(TAG, data.toString());

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();
        String target;

        if(mode == 1){// submit
            target = VM_public_ip + "setTemp";
        }else {// reset
            target = VM_public_ip + "recent";

        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, target, new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response.toString()");
                        Log.d(TAG, response.toString());
                        try {//TODO: check connection with sever
                            //current
                            int ctemp = response.getInt("current_temp");
                            int chum = response.getInt("current_hum");

                            int ONOFF = response.getInt("status");
                            int dtemp = response.getInt("desire_temp");

                            //on or off
                            chumidity.setText(Integer.toString(chum) + "%");
                            ctemperature.setText(Integer.toString(ctemp) + "C");


                            if (mode == 2) {
//                                desiredTemp = Integer.toString(dtemp);
//                                target_temperature.setText(desiredTemp + "C");
                                Log.d(TAG, "dtemp: " + response.getInt("desire_temp"));
                                seekbar.setProgress((float) dtemp / (maxTemp - minTemp) * seekbar.getMax());
                                updateTV(dtemp);

                                if (ONOFF == 1) {
                                    power.setText("Power ON");
                                    status = "ON";
                                } else {
                                    power.setText("Power OFF");
                                    status = "OFF";
                                }
                            }

                        } catch (JSONException e) {
                            Log.d(TAG, "Json format error");
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

    private void updateTV(int intTemp){
        if(lowerInt <= intTemp && intTemp <= upperInt){
            target_temperature.setText(intTemp + "C");
            if(intTemp > thresholdTemp){
                background.setBackgroundColor(Color.rgb(redWarm, greenWarm - 2 * intTemp, blueWarm - 2 * intTemp));
            }else {
                background.setBackgroundColor(Color.rgb(redCold + intTemp, greenCold + intTemp, blueCold ));
            }
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        try{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            upperInt = Integer.parseInt(sharedPref.getString("upperTempLimit", null));
            lowerInt = Integer.parseInt(sharedPref.getString("lowerTempLimit", null));
            deviceID = sharedPref.getString("typedDeviceID", null);

            Log.d("deviceID: ", deviceID);
            Log.d("upper: ", String.valueOf(upperInt));
            Log.d("lower: ", String.valueOf(lowerInt));
            update(2);

        }catch (Exception e){
            //first time open
            upperInt = 100;
            lowerInt = 0;

            deviceID = "None";
            desiredTemp = "26";
            status = "ON";

            Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
            startActivity(i);
        }
    }
}
