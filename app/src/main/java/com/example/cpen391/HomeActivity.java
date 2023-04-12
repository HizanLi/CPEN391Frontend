package com.example.cpen391;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.example.cpen391.setting.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class HomeActivity extends AppCompatActivity {
    public static String VM_public_ip;
    private final int maxTemp = 100, minTemp = 0;
    private final String TAG = "HomeActivity";
    private TextView ctemperature, chumidity, target_temperature, remainderContent;
    private Button power, submit, reset, history;
    CircularSeekBar seekbar;
    private int upperInt, lowerInt;
    private final int thresholdTemp = 30;
    private final int redWarm = 255, greenWarm = 265, blueWarm = 265;
    private final int redCold = 153, greenCold = 190, blueCold = 255;
    private static String sha256username, deviceID, desiredTemp;
    private ImageView setting;
    private ConstraintLayout background;
    private LinearLayout remainderaLyout;
    private int backgroundColor;
    private Handler handler = new Handler();
    private Spinner chooseDevice;
    private Runnable runnable = null;
    private int delay = 5000;

    private static ArrayList<String> spanItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG,"HOMENOW");
        VM_public_ip = getString(R.string.ipAddress);

        Bundle extras = getIntent().getExtras();

        sha256username= extras.getString("sha256username");

        backgroundColor = Color.rgb(0,0,0);
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

        }

        //Textview
        remainderContent = findViewById(R.id.remainderContent);
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

        remainderaLyout = findViewById(R.id.remainder_layout);

        seekbar = findViewById(R.id.seek_bar);

        seekbar.setCircleProgressColor(Color.rgb(232, 0, 0));
        seekbar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@Nullable CircularSeekBar circularSeekBar, float v, boolean b){

                if(lowerInt < upperInt){
                    float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                    int intTemp = (int)currentTemp;

                    updateBack(intTemp);
                }
            }

            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {

                //In settings, lowerInt > upperInt
                if( lowerInt > upperInt){
                    alert("Alert", "lowerInt > upperInt, jump to setting?");
                }else{
                    remainderaLyout.setVisibility(View.VISIBLE);

                    float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                    int intTemp = (int)currentTemp;

                    desiredTemp = Integer.toString(intTemp);

                    if(! (lowerInt <= intTemp && intTemp <= upperInt)){
                        Toast.makeText(HomeActivity.this, "Not in desired range", Toast.LENGTH_SHORT).show();
                        alert("Jump", "Not in desired Range, jump to setting?");
                    }else{
                        updateBack(intTemp);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar){}
        });

        updateBack(26);

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this,"View History", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this, HistoryActivity.class);
                i.putExtra("deviceID", deviceID);
                i.putExtra("sha256username", sha256username);
                i.putExtra("backgroundColor", backgroundColor);
                i.putExtra("desiredTemp", Integer.parseInt(desiredTemp));
                i.putExtra("delay", delay);
                startActivity(i);
            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(power.getText().toString().equalsIgnoreCase("Power OFF")){
                    desiredTemp = "0";
                    power.setText("Power ON");
                    seekbar.setProgress(0);
                    seekbar.setEnabled(false);
                    target_temperature.setText("OFF");
                    update(1);

                }else{
                    power.setText("Power OFF");
                    updateBack(lowerInt);
                    desiredTemp = Integer.toString(lowerInt);
                    target_temperature.setText(Integer.toString(lowerInt) + "C");
                    seekbar.setProgress((float) lowerInt / (maxTemp - minTemp) * seekbar.getMax());
                    seekbar.setEnabled(true);
                    update(1);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(power.getText().toString().equalsIgnoreCase("Power on")){
                    return;
                }
                remainderaLyout.setVisibility(View.INVISIBLE);
//                int sl = target_temperature.getText().toString().length();
//                desiredTemp = target_temperature.getText().toString().substring(0,sl - 1);

                int dt = Integer.parseInt(desiredTemp);
                if(lowerInt <= dt && dt <= upperInt){
                    update(1);
                }else {
                    Toast.makeText(HomeActivity.this, "Not in desired range", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(power.getText().toString().equalsIgnoreCase("Power on")){
                    return;
                }

                remainderaLyout.setVisibility(View.INVISIBLE);

                update(2);
            }
        });

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this,"Update Settings", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                i.putExtra("backgroundColor", backgroundColor);
                startActivity(i);
            }
        });


        chooseDevice = findViewById(id.deviceID_Spanner);
        setNewSpanItem();
        chooseDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String targerId = spanItems.get(i);
                Log.d(TAG, "targerId: '" + targerId+"'");
                deviceID = targerId;
                update(2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void alert(String title, String content){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Log.d(TAG,"alert seting");
                Intent i = new Intent(HomeActivity.this, SettingsActivity.class);
                i.putExtra("backgroundColor", backgroundColor);
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
        Log.d(TAG, "deviceID: " + deviceID);

        if(deviceID.equalsIgnoreCase("None")){
            alert("Setting", "Please Update Your Device Id");
            return;
        }

        HashMap<String, String> data = new HashMap<>();

        data.put("username", sha256username);
        data.put("device_id", deviceID);

        if(power.getText().toString().equalsIgnoreCase("Power OFF")){
            if(desiredTemp.length() == 1){
                desiredTemp = "0"+desiredTemp;
            }
            data.put("desire_temp", desiredTemp);
        }else{
            data.put("desire_temp", "0");
        }


        Log.d(TAG, "data.toString(): " +data.toString());

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();
        String target;

        if(mode == 1){// submit
            target = VM_public_ip + "setTemp_sw";
        }else {// reset mode == 2 or mode == 3
            target = VM_public_ip + "recent";
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, target, new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        try {//TODO: check connection with sever


                            int ctemp = response.getInt("current_temp");
                            int chum = response.getInt("current_hum");

                            chumidity.setText(Integer.toString(chum) + "%");
                            ctemperature.setText(Integer.toString(ctemp) + "C");

                            if(mode == 1){
                                int result = response.getInt("result");
                                Log.d(TAG, "result: "+ result);

                            } else {
                                int dtemp = response.getInt("desire_temp");
                                Log.d(TAG, "dtemp: " + response.getInt("desire_temp"));
                                seekbar.setProgress((float) dtemp / (maxTemp - minTemp) * seekbar.getMax());
                                updateBack(dtemp);
                            }

                            if (response.get("status").toString().equalsIgnoreCase("1")) {
                                //hardware Mode
                                seekbar.setEnabled(false);
                                power.setEnabled(false);
                                submit.setEnabled(false);
                                reset.setEnabled(false);
                                remainderContent.setText(R.string.reminder_hardware);
                                remainderContent.setVisibility(View.VISIBLE);
                            }else{
                                //software Mode
                                seekbar.setEnabled(true);
                                power.setEnabled(true);
                                submit.setEnabled(true);
                                reset.setEnabled(true);
                                remainderContent.setText(R.string.reminder_click);
                                //TODO
                                //What is remainderContent.setVisibility(View.?);
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
                        Log.d(TAG, "Mode On error: " + mode);
                        Log.d(TAG, "onErrorResponse in updates " + error.getMessage());
                    }
                });
        queue.add(request);
    }

    private void updateTVs(int colorRGB){
        TextView ctt = findViewById(id.ctemperature_title);
        ctt.setTextColor(colorRGB);

        TextView ct = findViewById(id.ctemperature);
        ct.setTextColor(colorRGB);

        TextView cht = findViewById(id.chumidity_title);
        cht.setTextColor(colorRGB);

        TextView ch = findViewById(id.chumidity);
        ch.setTextColor(colorRGB);

        TextView tt = findViewById(id.target_temperature);
        tt.setTextColor(colorRGB);

        TextView ttt = findViewById(id.hum_title);
        ttt.setTextColor(colorRGB);

        power.setBackgroundColor(colorRGB);
        reset.setBackgroundColor(colorRGB);
        submit.setBackgroundColor(colorRGB);
        history.setBackgroundColor(colorRGB);

    }

    private void updateBack(int intTemp){

        if(lowerInt <= intTemp && intTemp <= upperInt){
            desiredTemp = Integer.toString(intTemp);
            if(intTemp<10){
                target_temperature.setText("0" + intTemp + "C");

            }else {
                target_temperature.setText(intTemp + "C");

            }
            if(intTemp > thresholdTemp){
                updateTVs(Color.rgb(227,38,54));
                backgroundColor = Color.rgb(redWarm, greenWarm -  intTemp, blueWarm - intTemp);
                background.setBackgroundColor(backgroundColor);
            }else {
                updateTVs(Color.rgb(15,82,186));
                backgroundColor = Color.rgb(redCold + intTemp, greenCold + intTemp, blueCold);
                background.setBackgroundColor(backgroundColor);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try{
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Log.d(TAG, sharedPref.getAll().toString());
            upperInt = Integer.parseInt(sharedPref.getString("upperTempLimit", null));
            lowerInt = Integer.parseInt(sharedPref.getString("lowerTempLimit", null));
            deviceID = sharedPref.getString("typedDeviceID", null);
            delay = Integer.parseInt(sharedPref.getString("refresh_interval", "")) * 1000;
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
            updateBack(26);

            alert("Welcome", "You have not complete settings, jump to setting?");
        }
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                update(3);
            }
        }, delay);

        setNewSpanItem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
    }

    private void setNewSpanItem(){
        String re = readFromJson(this);

        if(re != null){
            Log.d(TAG, "reading result: " + re);
            try {
                JSONObject jobj = new JSONObject(re);
                JSONArray jary = jobj.getJSONArray("idsArray");
                Log.d(TAG,jary.toString());
                spanItems = new ArrayList<String>();

                for (int i=0;i<jary.length();i++){
                    spanItems.add(jary.getString(i));
                }
                Log.d(TAG, spanItems.toString());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spanItems);
                //set the spinners adapter to the previously created one.
                chooseDevice.setAdapter(adapter);

                int index = getIndex(chooseDevice, deviceID);
                chooseDevice.setSelection(index);
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        spanItems = new ArrayList<String>();
        spanItems.add("None");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spanItems);
        //set the spinners adapter to the previously created one.
        chooseDevice.setAdapter(adapter);
        int index = getIndex(chooseDevice, deviceID);
        chooseDevice.setSelection(index);
    }
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }
    private String readFromJson(Context applicationContext){
        File file = new File(String.valueOf(applicationContext.getFilesDir()), "allDevice.json");
        if(file.exists() && !file.isDirectory()) {
            try{

                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                bufferedReader.close();
                // This response will have Json Format String
                String result = stringBuilder.toString();

                return result;
            }catch (IOException e){
                // Can not read file
                e.printStackTrace();
                return null;
            }
        }
        // File not exist
        return null;
    }
}
