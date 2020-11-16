package com.example.recordwifiinfo.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recordwifiinfo.MyService;
import com.example.recordwifiinfo.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent(this, MyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            openLocationService();
        }
        List<String> permissions=new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, android.Manifest
                .permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (ContextCompat.checkSelfPermission(this, android.Manifest
                    .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        if (permissions!=null&&permissions.size()>0){
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), 1);
        } else {
            startFloatingService();
        }
        textView = findViewById(R.id.wifiInfo_textView);
        button = findViewById(R.id.showInfo_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readWifiInfo();
            }
        });
    }
    public void openLocationService() {

        LocationManager manager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        boolean isGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPS && !isNetwork) {
            Toast.makeText(this, "请开启定位服务!", Toast.LENGTH_LONG).show();
            Intent intent1 = new Intent();
            intent1.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent1);
        }
    }



    @SuppressLint("NewApi")
    public void startFloatingService() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 0);
        } else {
            startService(intent);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                startService(intent);
            }
        }

    }

    private void readWifiInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream inputStream = MainActivity.this.openFileInput("data");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String temp = "";
            while ((temp = reader.readLine()) != null) {
                stringBuilder.append(temp + "\n");
            }
            textView.setText(stringBuilder.toString());
            reader.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length>0){
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "必须同意读写权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
            }
            startFloatingService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }


}
