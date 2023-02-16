package com.example.cpen391;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
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

    public final String VM_public_ip = "http://3.96.148.29:8000/";
    private final String TAG = "ServerConnection";

    public volatile int loginDone = 0;
    public String checkConnection(String SHA256_username, String SHA256_password, Context context){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", SHA256_username);
        params.put("password", SHA256_password);
        return "AllDone";
    }
    /*
    *  Return:
    *  int 1 on success (correct password and username)
    *  int 0 on fail (incorrect password or username)
    *  int -1 on fail (no response from server)
    * */
    public int login(String SHA256_username, String SHA256_password, Context context){

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", SHA256_username);
        params.put("password", SHA256_password);

        int sleepCounter = 0;
        while (loginDone != 1) {
            Log.d(TAG, "sleepCounter: "+ sleepCounter);
            Log.d(TAG, "loginDone: "+ loginDone);
            sleepCounter += 1;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            if (sleepCounter > 10) {
                return -1;
            }
        }
        return 1;
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
}
