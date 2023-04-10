package com.example.cpen391;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cpen391.chat.ChatActivity;
import com.example.cpen391.setting.SettingsActivity;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Login page
public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
//    public final String VM_public_ip = getString(R.string.ipAddress);

    public static String VM_public_ip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VM_public_ip = getString(R.string.ipAddress);

        TextView usernameTV =(TextView) findViewById(R.id.email);
        TextView passwordTV =(TextView) findViewById(R.id.password);

        MaterialButton loginbtn = (MaterialButton) findViewById(R.id.loginbtn);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting data from our edit text.
                String userName = usernameTV.getText().toString().trim();
                String password = passwordTV.getText().toString().trim();

                // checking if the entered text is empty or not.
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Please enter email or password", Toast.LENGTH_SHORT).show();
                }else{
//                    login(getSha256Hash(userName.trim()), getSha256Hash(password.trim()));

                    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
                    Pattern pattern = Pattern.compile(emailRegex);
                    Matcher matcher = pattern.matcher(userName.trim());
                    if(matcher.matches()){
                        login(getSha256Hash(userName.trim()), getSha256Hash(password.trim()));
                    }else{
                        Toast.makeText(MainActivity.this, "Please enter you email", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        TextView forgotpass = findViewById(R.id.forgotpass);
        forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = usernameTV.getText().toString();
                Log.d(TAG, "Reset Password");
                resetPassword(getSha256Hash(userName.trim()));
            }
        });
    }

    private void login(String username, String password){
        HashMap<String, String> params = new HashMap<String, String>();

        //TODO: check with server about Json format

        params.put("username", getSha256Hash(username));
        params.put("password", getSha256Hash(password));
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "login", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response.toString()");
                        Log.d(TAG, response.toString());
                        try {
                            int serverResponse = response.getInt("result");
                            if(serverResponse == 1){

                                Log.d(TAG, "Correct Password");
//                                Toast.makeText(MainActivity.this,"LOGIN SUCCESSFUL",Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(MainActivity.this, HomeActivity.class);

                                i.putExtra("username", username);
                                i.putExtra("sha256username", getSha256Hash(username));

                                startActivity(i);
                            }else{
                                Log.d(TAG, "Incorrect Password");
                                Toast.makeText(MainActivity.this,"Incorrect Password or Username", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this,"Server Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse " + error.getMessage());
                        alert("Network Error","Please try again later. Or contact us at xxx@gmail.com");
                        Log.d(TAG,"gg");
                        Toast.makeText(MainActivity.this,"Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(request);
    }

    private void alert(String title, String content){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        builder.show();
    }

    private void resetPassword(String SHA256_username){
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", SHA256_username);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.start();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VM_public_ip + "resetPassword", new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response.toString()");
                        Log.d(TAG, response.toString());
                        Toast.makeText(MainActivity.this,"New Password Will be send to your email.",Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this,"Network Error", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse " + error.getMessage());
                    }
                });
        queue.add(request);
    }

    public String getSha256Hash(String password) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
            digest.reset();
            return bin2hex(digest.digest(password.getBytes()));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }
}

