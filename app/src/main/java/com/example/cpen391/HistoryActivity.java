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
    public final String VM_public_ip = "http://3.96.148.29:8000/";
    BarChart chartTemp, chartHum;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        chartTemp = findViewById(R.id.barcharttemp);
        chartHum = findViewById(R.id.barcharthum);

        JSONObject test= new JSONObject();
        JSONArray tempReading = new JSONArray();
        tempReading.put(10);
        tempReading.put(11);
        tempReading.put(12);
        tempReading.put(13);
        tempReading.put(14);
        tempReading.put(15);
        tempReading.put(16);
        tempReading.put(17);
        tempReading.put(18);
        tempReading.put(19);

        JSONArray readings = new JSONArray();
        readings.put(10);
        readings.put(11);
        readings.put(12);
        readings.put(13);
        readings.put(14);
        readings.put(15);
        readings.put(16);
        readings.put(17);
        readings.put(18);
        readings.put(19);
        try {
            test.put("temperature_history", tempReading);
            test.put("humidity_history", readings);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        updateChart(test, Hum);
        updateChart(test, Temp);

//        getHistory();
    }

    private void getHistory(){
        HashMap<String, String> data = new HashMap<>();
        data.put("username", "SHA256_username");
        data.put("deviceId", "deviceId");
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "history", new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response.toString()");
                        Log.d(TAG, response.toString());
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

            for(int i = 0; i < 10; i ++){
                readingBarEntry.add(new BarEntry(i, readingJSONArray.getInt(i)));
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


