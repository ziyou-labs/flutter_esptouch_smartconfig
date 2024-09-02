package com.ziyou.esptouch_smartconfig;

import androidx.annotation.NonNull;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.location.LocationManager;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.EventChannel;


import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.EsptouchTaskParameter;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.TouchNetUtil;

public class FlutterEventChannelHandler implements EventChannel.StreamHandler {
    private static final String TAG = "ESPTouchSmartConfig";
    private static final String CHANNEL_NAME= "esptouch_smartconfig";

    private final Context context;

    private MainThreadEventSink eventSink;
    private EsptouchAsyncTask esptouchAsyncTask;

    FlutterEventChannelHandler(Context context) {
        this.context = context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        Log.d(TAG, "Event Listener is triggered");
        Map<String, Object> map = (Map<String, Object>) o;
        String ssid = (String) map.get("ssid");
        String bssid = (String) map.get("bssid");
        String password = (String) map.get("password");
        String deviceCount = (String) map.get("deviceCount");
        String broadcast = (String) map.get("isBroad");

        Log.d(TAG, String.format("Received stream configuration arguments: SSID: %s, BBSID: %s, Password: %s", ssid, bssid, password));
        
        if(esptouchAsyncTask != null) {
            esptouchAsyncTask.cancelEsptouch();
        }
        this.eventSink = new MainThreadEventSink(eventSink);
        esptouchAsyncTask = new EsptouchAsyncTask(context, this.eventSink);
        esptouchAsyncTask.execute(ssid, bssid, password, deviceCount, broadcast);
    }

    @Override
    public void onCancel(Object o) {
        Log.d(TAG, "Cancelling stream with configuration arguments" + o);
        this.eventSink.dispose();
        this.eventSink = null;
        esptouchAsyncTask.cancelEsptouch();
        esptouchAsyncTask = null;
    }
}