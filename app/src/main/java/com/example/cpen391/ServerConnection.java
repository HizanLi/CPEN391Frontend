package com.example.cpen391;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;

public class ServerConnection {

    public final String VM_public_ip = "http://15.222.207.245:8000/";
    private final String TAG = "ServerConnection";

    public String checkConnection(String SHA256_username, String SHA256_password, Context context){

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", SHA256_username);
        params.put("password", SHA256_password);

        return "here";
    }

    public int login(String SHA256_username, String SHA256_password){

        return -1;
    }

    public int getCurrentData(String SHA256_username, String SHA256_password){

        return -1;
    }

    public int getHistoryData(String SHA256_username, String SHA256_password){

        return -1;
    }

    public int adjustTemp(String SHA256_username, String SHA256_password, double temperature){

        return -1;
    }

    private void sendRequestToServer(HashMap<String, String> data, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.start();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "login", new JSONObject(data),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Unable to reload credit");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse updateUI " + "Error: " + error.getMessage());
                    }
                });
        queue.add(request);
    }
}
