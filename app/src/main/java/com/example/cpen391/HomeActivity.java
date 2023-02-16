package com.example.cpen391;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cpen391.R.id;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class HomeActivity extends AppCompatActivity {

    private int maxTemp, minTemp, roomTemp;

    private TextView userNameTV;
    private final String TAG = "HomeActivity";
    // button for logout
    private Button logoutBtn;
    private TextView ctemperature, chumidity, tips, target_temperature;
    private Button power, submit, clear, history;
    CircularSeekBar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        minTemp = 0;
        maxTemp = 100;

        target_temperature = findViewById(R.id.target_temperature);
        ctemperature = findViewById(R.id.ctemperature);
        chumidity = findViewById(R.id.chumidity);
        tips = findViewById(R.id.tips);

        power = findViewById(id.power);
        submit = findViewById(R.id.submit);
        clear = findViewById(R.id.clear);
        history = findViewById(R.id.viewHistory);

        seekbar = findViewById(R.id.seek_bar);
        seekbar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(@Nullable CircularSeekBar circularSeekBar, float v, boolean b){
                Log.d(TAG, "Progress: " + seekbar.getProgress());
                float currentTemp = ((maxTemp - minTemp) / seekbar.getMax()) * seekbar.getProgress();
                Log.d(TAG, "currentTemp: "+ currentTemp);
                target_temperature.setText((int)currentTemp + "C");
            }

            @Override
            public void onStopTrackingTouch(@Nullable CircularSeekBar circularSeekBar) {

            }

            @Override
            public void onStartTrackingTouch(@Nullable CircularSeekBar circularSeekBar){

            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(HomeActivity.this,"View History", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(i);
            }
        });

        power.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}
