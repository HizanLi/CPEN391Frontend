package com.example.cpen391;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity{
    private final int Temp = 1, Hum = 2;
    private final String TAG = "HistoryActivity";
//    public final String VM_public_ip = getString(R.string.ipAddress);
    public static String VM_public_ip;

    private static String sha256username, deviceID;
BarChart chartTemp, chartHum;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        VM_public_ip = getString(R.string.ipAddress);

        chartTemp = findViewById(R.id.barcharttemp);
        chartHum = findViewById(R.id.barcharthum);

        Bundle extras = getIntent().getExtras();

        deviceID= extras.getString("deviceID");
        sha256username= extras.getString("sha256username");

        getHistory();
    }

    private void getHistory(){
        HashMap<String, String> data = new HashMap<>();
        data.put("username", sha256username);
        data.put("device_id", deviceID);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();

        Log.d(TAG, data.toString());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "history_sw", new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.d(TAG, "response.toString()");
//                        Log.d(TAG, response.toString());
                        updateChart(response, Temp);
                        updateChart(response, Hum);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HistoryActivity.this,"Network Error", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse " + error.getMessage());
                    }
                });
        queue.add(request);
    }

    private void updateChart(JSONObject data, int chartType){
        try {
            JSONArray readingJSONArray;
            BarDataSet readingBarDataSet;

            ArrayList<BarEntry> readingBarEntry = new ArrayList<BarEntry>();
            if(chartType == Temp){
                readingJSONArray = data.getJSONArray("temperature_history");
            }else{
                readingJSONArray = data.getJSONArray("humidity_history");
            }
            int arrayLength = readingJSONArray.length();

            if(arrayLength >= 10){
                for(int i = 0; i < 10; i ++){
                    readingBarEntry.add(new BarEntry(i, readingJSONArray.getInt(i)));
                }
            }else {
                for(int i = 0; i < arrayLength; i ++){
                    readingBarEntry.add(new BarEntry(i, readingJSONArray.getInt(i)));
                }
            }


            chartHum.animateY(10);
            chartTemp.animateY(10);

            if(chartType == Temp){
                readingBarDataSet = new BarDataSet(readingBarEntry, "Temperature");
            }else{
                readingBarDataSet = new BarDataSet(readingBarEntry, "Humidity");
            }

            BarData dataTemp = new BarData(readingBarDataSet, readingBarDataSet);
            readingBarDataSet.setColors(ColorTemplate.COLORFUL_COLORS);

            if(chartType == Temp){
                chartTemp.setData(dataTemp);
            }else{
                chartHum.setData(dataTemp);
            }

        } catch (JSONException e) {
            Log.d(TAG, "Format Error");
        }
    }
}


