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
    private final String TAG = "HistoryActivity";
    public final String VM_public_ip = "http://3.96.148.29:8000/";
    BarChart chartTemp, chartHum;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        chartTemp = findViewById(R.id.barcharttemp);
        chartHum = findViewById(R.id.barcharthum);

        getHistory();
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
                        try {
                            JSONArray temps = response.getJSONArray("temperature_history");
                            ArrayList<BarEntry> NoOfTemps = new ArrayList<BarEntry>();
                            JSONArray hums = response.getJSONArray("humidity_history");
                            ArrayList<BarEntry> NoOfHums = new ArrayList<BarEntry>();
                            for(int i = 0; i < 10; i ++){
                                NoOfTemps.add(new BarEntry(i, temps.getInt(i)));
                                NoOfHums.add(new BarEntry(i, hums.getInt(i)));
                            }
                            BarDataSet bardatasetTemps = new BarDataSet(NoOfTemps, "Temperature");
                            BarDataSet bardatasetHums = new BarDataSet(NoOfHums, "Humidity");

                            chartHum.animateY(10);
                            chartTemp.animateY(10);
                            BarData dataTemp = new BarData(bardatasetTemps, bardatasetTemps);
                            BarData dataHum = new BarData(bardatasetHums, bardatasetHums);
                            bardatasetTemps.setColors(ColorTemplate.COLORFUL_COLORS);
                            bardatasetHums.setColors(ColorTemplate.COLORFUL_COLORS);
                            chartHum.setData(dataTemp);
                            chartTemp.setData(dataHum);

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
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
    }}


