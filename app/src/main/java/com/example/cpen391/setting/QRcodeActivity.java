package com.example.cpen391.setting;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.cpen391.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QRcodeActivity extends CaptureActivity {
    private final String TAG = "QRcodeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qrcode);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);      // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
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