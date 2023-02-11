package com.example.cpen391;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class ServerConnection {

    public final String VM_public_ip = "http://15.222.207.245:8000/";
    private final String TAG = "ServerConnection";

    public int checkConnection(String SHA256_username, String SHA256_password){

        return -1;
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

    private int sendRequestToServer(JSONObject request){

        return -1;
    }
}
