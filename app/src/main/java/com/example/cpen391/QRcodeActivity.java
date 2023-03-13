package com.example.cpen391;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRcodeActivity extends AppCompatActivity {
    private final String TAG = "QRcodeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        // if the intentResult is null then
        // toast a message as "cancelled"
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();

                Intent intent=new Intent();
                intent.putExtra("MESSAGE", "Failed");
                setResult(2,intent);
                finish();//finishing activit

            } else {
                Intent intent=new Intent();
                intent.putExtra("MESSAGE", intentResult.getContents());
                setResult(2,intent);
                finish();//finishing activit
            }
        } else {
            Intent intent=new Intent();
            intent.putExtra("MESSAGE", "Failed");
            setResult(2,intent);
            finish();//finishing activit
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}