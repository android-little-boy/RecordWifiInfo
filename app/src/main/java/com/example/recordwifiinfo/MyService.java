package com.example.recordwifiinfo;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.example.recordwifiinfo.activity.MainActivity;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class MyService extends Service {

    WindowManager.LayoutParams layoutParams;
    WindowManager windowManager;
    TextView textView;
    LinearLayout floatingView;
    com.example.recordwifiinfo.model.WifiInfo wifiInfo;
    WifiChangeReceiver wifiChangeReceiver;


    @Override
    public void onCreate() {
        super.onCreate();
        wifiInfo = new com.example.recordwifiinfo.model.WifiInfo();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("my_service", "记录wifi信息service通知",
                    NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(this, "my_service")
                .setContentTitle("记录WiFi信息")
                .setContentIntent(pi)
                .build();
        startForeground(1, notification);
        registerReceiver();
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        wifiChangeReceiver = new WifiChangeReceiver();
        registerReceiver(wifiChangeReceiver, intentFilter);
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

    private int getWifiStrength() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        int strength = info.getRssi();
        return strength;
    }

    private void recordWifiInfo(int action) {
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("NewApi")
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            // 获取WindowManager服务
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            // 新建悬浮窗控件
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            floatingView = (LinearLayout) layoutInflater.inflate(R.layout.floating_window, null);
            floatingView.setOnTouchListener(new FloatingOnTouchListener());
            textView = floatingView.findViewById(R.id.textView2);
            textView.setSelected(true);

            // 设置LayoutParam
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.width = 400;
            layoutParams.height = 200;
            layoutParams.x = 300;
            layoutParams.y = 300;

            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(floatingView, layoutParams);
        }

    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        int startX;
        int startY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    int lastX = (int) event.getRawX();
                    int lastY = (int) event.getRawY();
                    if (Math.abs(startX - lastX) < 1.5 && Math.abs(startY - lastY) < 1.5) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;

                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(v, layoutParams);
                    break;
            }
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wifiChangeReceiver != null) {
            unregisterReceiver(wifiChangeReceiver);
        }
        windowManager.removeView(floatingView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}