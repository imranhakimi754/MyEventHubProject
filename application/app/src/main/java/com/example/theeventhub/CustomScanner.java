// CustomScannerActivity.java
package com.example.theeventhub;


import com.journeyapps.barcodescanner.CaptureActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class CustomScanner extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}

