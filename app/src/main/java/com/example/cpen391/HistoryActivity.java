package com.example.cpen391;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity{
    private final int Temp = 1, Hum = 2;
    private final String TAG = "HistoryActivity";
//    public final String VM_public_ip = getString(R.string.ipAddress);
    public static String VM_public_ip;

    private static String sha256username, deviceID;

    private int desiredTemp;

BarChart chartTemp, chartHum;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        VM_public_ip = getString(R.string.ipAddress);

        Bundle extras = getIntent().getExtras();

        int backgroundColor = extras.getInt("backgroundColor");
        desiredTemp = extras.getInt("desiredTemp");

        ConstraintLayout history_background = findViewById(R.id.history_background);
        history_background.setBackgroundColor(backgroundColor);

        chartTemp = findViewById(R.id.barcharttemp);
        chartHum = findViewById(R.id.barcharthum);

        deviceID= extras.getString("deviceID");
        sha256username= extras.getString("sha256username");

//        getHistory();


//        ArrayList<Integer> test = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
//        dataAnalyze(test, temp);
//
        ArrayList<Integer> test2 = new ArrayList<>(Arrays.asList(40, 23, 41, 42, 43, 44, 45, 46, 47));
        temperatureAnalyze(test2);

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

    private void temperatureAnalyze(ArrayList<Integer> data){
        int dataSize = data.size();
        int mostRecentReading = data.get(dataSize - 1);

        if(mostRecentReading == desiredTemp){
            updateAnalysis(0.0, 0.0);
            return;
        }

        ArrayList<Double> stepGradiant = new ArrayList<>();
        int stepGradiantSize = dataSize - 1;

        for (int i = 0; i < stepGradiantSize; i++) {
            double grad = (double) data.get(i+1) - (double) data.get(i);
            stepGradiant.add(grad);
        }
        Log.d(TAG, stepGradiant.toString());

        /* The temp will not increase suddenly, but could decrease suddenly (i.e., when user opens the container)*/
        if(stepGradiantSize > 2){
            double max = Collections.max(stepGradiant);
            stepGradiant.remove(max);
            double min = Collections.min(stepGradiant);
            stepGradiant.remove(min);
        }
        Log.d(TAG, stepGradiant.toString());
        stepGradiantSize = stepGradiant.size();

        double avgGrad = 0.0;
        for (int i = 0; i < stepGradiantSize; i++) {
            avgGrad += stepGradiant.get(0);
        }
        avgGrad = avgGrad / stepGradiantSize;


        Log.d(TAG, "avgGrad: " + avgGrad);
        if( mostRecentReading == 0){
            return;
        }

        double timeNeed = -1;

        Log.d(TAG, "a: " + (mostRecentReading < desiredTemp) );
        if(mostRecentReading < desiredTemp){
            if(avgGrad > 0.0){
                //User want the temp to go up, and the temp inside the container is indeed goes up
                timeNeed = Math.ceil((desiredTemp - mostRecentReading) / avgGrad);
            }else{
                //User want the temp to go up, and the temp inside the container is going down

                //nothing need to be done here
            }
        }else{
            if(avgGrad < 0){
                //User want the temp to go down, and the temp inside the container is indeed goes down
                timeNeed = Math.ceil((mostRecentReading - desiredTemp ) / avgGrad);
            }else{
                //User want the temp to go down, and the temp inside the container is going up

                //nothing need to be done here
            }
        }
        updateAnalysis(avgGrad, timeNeed);
    }

    private void updateAnalysis(double avgGrad,double timeNeeded){
        String result;
        if(Math.abs(timeNeeded - 0.0) < 0.000001d){
            result = "Congratulations! The environment temperature is at your setting.\n";
        }else if (timeNeeded > 0.0){
            if(avgGrad < 0.0){
                result = "Congratulations! The environment temperature is decreasing.\n";
            }else{
                result = "Congratulations! The environment temperature is increasing.\n";
            }
            result += "It take " + timeNeeded + " unit to reach your setting.\n";
        }else{
            result = "Something wrong happened, could not estimate.\n";
        }
        TextView analysis_content = findViewById(R.id.analysis_content);
        analysis_content.setText(result);
    }


}


