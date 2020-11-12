package com.example.recordwifiinfo.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recordwifiinfo.R;

public class MainActivity extends AppCompatActivity {

    WifiChangeReceiver wifiChangeReceiver;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, android.Manifest
                .permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest
                    .permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            registerReceiver();
        }
        textView = findViewById(R.id.wifiInfo_textView);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        wifiChangeReceiver = new WifiChangeReceiver();
        registerReceiver(wifiChangeReceiver, intentFilter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "必须同意读写权限才能使用本程序", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            registerReceiver();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiChangeReceiver != null) {
            unregisterReceiver(wifiChangeReceiver);
        }
    }

    class WifiChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(MainActivity.this, "触发了", Toast.LENGTH_SHORT).show();
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//            WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
//            String bssid = networkInfo.getExtraInfo();
            NetworkInfo.State state = networkInfo.getState();
            if (state == NetworkInfo.State.CONNECTED) {
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo info = wifiManager.getConnectionInfo();
                int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                textView.setText("强度"+strength);
            }
            if (state == NetworkInfo.State.DISCONNECTED) {
                textView.setText("断开");
            }
//            if (state==NetworkInfo.State.CONNECTING){
//                textView.setText("连接中");
//            }
//            if (state==NetworkInfo.State.DISCONNECTING){
//                textView.setText("断开中");
//            }
        }
    }

}
