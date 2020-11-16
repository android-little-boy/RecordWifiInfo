package com.example.recordwifiinfo.model;

import java.util.Date;

public class WifiInfo {
    String name;
    String connectDate;
    String disconnectDate;
    int wifiStrength;
    //记录信息时判断是断开还是连接
    public final static int CONNECT = 1;
    public final static int DISCONNECT = 0;

    public WifiInfo() {
        name = "";
        connectDate = "";
        disconnectDate = "";
        wifiStrength = 0;
    }

    public String getName() {
        return name;
    }

    public String getConnectDate() {
        return connectDate;
    }

    public String getDisconnectDate() {
        return disconnectDate;
    }

    public int getWifiStrength() {
        return wifiStrength;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConnectDate(String connectDate) {
        this.connectDate = connectDate;
    }

    public void setDisconnectDate(String disconnectDate) {
        this.disconnectDate = disconnectDate;
    }

    public void setWifiStrength(int wifiStrength) {
        this.wifiStrength = wifiStrength;
    }
}
