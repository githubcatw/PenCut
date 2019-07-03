package com.nudev.pencut;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DPIGetter.resetDPI((WindowManager) getSystemService(WINDOW_SERVICE));
        final Button start = findViewById(R.id.start);
        Button stop = findViewById(R.id.stop);
        Button drawOver = findViewById(R.id.drawover);
        Button changeDPI = findViewById(R.id.changedpi);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (Settings.canDrawOverlays(getApplicationContext())){
                        start();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please permit drawing over apps.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    start();
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop();
            }
        });

        drawOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    checkDrawOverlayPermission();
                } else {
                    Toast.makeText(getApplicationContext(), "You don't need it.\nYou run Android <6.0.\nClick \"Start\" and enjoy!", Toast.LENGTH_LONG).show();
                }
            }
        });

        changeDPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DPIGetter.startDPIChangeDialog(MainActivity.this);
            }
        });


    }

    public void start(){
        if(!isMyServiceRunning(OverlayService.class)){
            startService(new Intent(this, OverlayService.class));
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Toast.makeText(getApplicationContext(), "The overlay will show up under the notification bar.\nThis is an API limitation put by Google.", Toast.LENGTH_LONG).show();
        }
    }

    public void stop(){
        if(isMyServiceRunning(OverlayService.class)){
            stopService(new Intent(this, OverlayService.class));
            OverlayService.removeView();
        }
    }

    /** code to post/handler request for permission */
    public final static int REQUEST_CODE = 3000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(), "Couldn't find overlay permission activity.\nPlease permit drawing over apps manually through the device settings.", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
