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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    WifiChangeReceiver wifiChangeReceiver;
    TextView textView;
    Button button;
    com.example.recordwifiinfo.model.WifiInfo wifiInfo;

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
        button = findViewById(R.id.showInfo_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readWifiInfo();
            }
        });
        wifiInfo = new com.example.recordwifiinfo.model.WifiInfo();
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

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
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

    int getWifiStrength() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        int strength = info.getRssi();
        return strength;
    }

    class WifiChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (state == NetworkInfo.State.CONNECTED) {
                    if (!networkInfo.getExtraInfo().equals(wifiInfo.getName())) {
                        wifiInfo.setName(networkInfo.getExtraInfo());
                        wifiInfo.setConnectDate(dateFormat.format(new Date()));
                        wifiInfo.setWifiStrength(getWifiStrength());
                        recordWifiInfo(com.example.recordwifiinfo.model.WifiInfo.CONNECT);
                    }
                    wifiInfo.setDisconnectDate("");
                    wifiInfo.setWifiStrength(getWifiStrength());
                    textView.setText("连接：" + wifiInfo.getName());
                }
                if (state == NetworkInfo.State.DISCONNECTED) {
                    if (!"".equals(wifiInfo.getName())) {
                        wifiInfo.setDisconnectDate(dateFormat.format(new Date()));
                        recordWifiInfo(com.example.recordwifiinfo.model.WifiInfo.DISCONNECT);
                        Log.d("ddaaas", "onReceive: " + wifiInfo.getName());
                    }
                    textView.setText("断开" + wifiInfo.getName());
                    wifiInfo.setName("");
                }
            }
            if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
                wifiInfo.setWifiStrength(getWifiStrength());
            }

        }
    }

    void recordWifiInfo(int action) {
        if (action == com.example.recordwifiinfo.model.WifiInfo.CONNECT) {
            StringBuilder connectWifiInfo = new StringBuilder();
            connectWifiInfo.append("连接：" + wifiInfo.getName() + "\n");
            connectWifiInfo.append("连接时间：" + wifiInfo.getConnectDate() + "\n");
            connectWifiInfo.append("信号强度：" + wifiInfo.getWifiStrength() + "\n");
            try {
                FileOutputStream outputStream = openFileOutput("data", MODE_APPEND);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                try {
                    bufferedWriter.write(connectWifiInfo.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (action == com.example.recordwifiinfo.model.WifiInfo.DISCONNECT) {
            StringBuilder disconnectWifiInfo = new StringBuilder();
            disconnectWifiInfo.append("断开：" + wifiInfo.getName() + "\n");
            disconnectWifiInfo.append("断开时间：" + wifiInfo.getDisconnectDate() + "\n");
            disconnectWifiInfo.append("信号强度：" + wifiInfo.getWifiStrength() + "\n");
            try {
                FileOutputStream outputStream = openFileOutput("data", MODE_APPEND);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                try {
                    bufferedWriter.write(disconnectWifiInfo.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedWriter != null) {
                        try {
                            bufferedWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
