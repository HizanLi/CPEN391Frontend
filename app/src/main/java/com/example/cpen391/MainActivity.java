package com.example.cpen391;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView usernameTV =(TextView) findViewById(R.id.username);
        TextView passwordTV =(TextView) findViewById(R.id.password);

        MaterialButton loginbtn = (MaterialButton) findViewById(R.id.loginbtn);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on below line we are getting data from our edit text.
                String userName = usernameTV.getText().toString();
                String password = passwordTV.getText().toString();
//                Log.d("userName sha256-ed: ", getSha256Hash(userName));
//                Log.d("password sha256-ed: ", getSha256Hash(password));

                Toast.makeText(MainActivity.this, "Send", Toast.LENGTH_SHORT).show();

                ServerConnection server = new ServerConnection();
                String result = server.checkConnection("1", "2", getApplicationContext());
                Log.d(TAG, result);
                Log.d(TAG, "Returned");
//                // checking if the entered text is empty or not.
//                if (TextUtils.isEmpty(userName) && TextUtils.isEmpty(password)) {
//                    Toast.makeText(MainActivity.this, "Please enter user name and password", Toast.LENGTH_SHORT).show();
//                }
//
//                if(userName.equals("admin") && password.equals("admin")){
//                    //correct
//                    Toast.makeText(MainActivity.this,"LOGIN SUCCESSFUL",Toast.LENGTH_SHORT).show();
//                    Intent i = new Intent(MainActivity.this, HomeActivity.class);
//                    i.putExtra("username", userName);
//                    startActivity(i);
//                }else{
//                    Toast.makeText(MainActivity.this,"Incorrect Password or Username",Toast.LENGTH_SHORT).show();
//                }
            }
        });
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

