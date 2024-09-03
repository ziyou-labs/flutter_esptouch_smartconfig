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


import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.TouchNetUtil;

public class EsptouchAsyncTask extends AsyncTask<String, IEsptouchResult, List<IEsptouchResult>> {
    private static final String TAG = "ESPTouchSmartConfig#EsptouchAsyncTask";
    private final Object mLock = new Object();
    private Context context;
    private IEsptouchTask mEsptouchTask;
    MainThreadEventSink eventSink;
    EsptouchAsyncTask( Context context, MainThreadEventSink eventSink) {
        this.context = context;
        this.eventSink = eventSink;
    }

    void cancel() {
        cancel(true);
        if (mEsptouchTask != null) {
            mEsptouchTask.interrupt();
        }
        if(eventSink != null) {
            eventSink.endOfStream();
        }
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected List<IEsptouchResult> doInBackground(String... params) {
        int taskResultCount;
        synchronized (mLock) {
            boolean broadcast = false;
            String apSsid = params[0];
            String apBssid = params[1];
            String apPassword = params[2];
            String deviceCountData = params[3];
            String broadcastData = params[4];
            Log.d(TAG, String.format("Received stream configuration arguments: SSID: %s, BBSID: %s, Password: %s, $s, $s", apSsid, apBssid, apPassword,deviceCountData,broadcastData));
            if (broadcastData.equals("YES")) broadcast = true;
            taskResultCount = deviceCountData.length() == 0 ? -1 : Integer.parseInt(deviceCountData);
            mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, context);
            mEsptouchTask.setPackageBroadcast(broadcast);
            IEsptouchListener listener = new IEsptouchListener() {
                @Override
                public void onEsptouchResultAdded(IEsptouchResult result) {
                    publishProgress(result);
                }
            };
            mEsptouchTask.setEsptouchListener(listener);
        }
        return mEsptouchTask.executeForResults(taskResultCount);
    }
    
    @Override
    protected void onProgressUpdate(IEsptouchResult... values){
        IEsptouchResult result = values[0];
        Log.d(TAG, "Received progress update length" + values.length);
        for (IEsptouchResult touchResult : values) {
            Log.d(TAG, "Received progress update data");
            Log.d(TAG, "Is success : " + touchResult.isSuc());
            Log.d(TAG, "Is cancelled : " + touchResult.isCancelled());
            Log.d(TAG, "BSSID : " + touchResult.getBssid());
            Log.d(TAG, "IP : " + touchResult.getInetAddress().getHostAddress());
        }
        //Map<String, String> sink = new HashMap<>();
        //sink.put("bssid", result.getBssid());
        //sink.put("ip", result.getInetAddress().getHostAddress());
        //eventSink.success(sink);
    }

    @Override
    protected void onPostExecute(List<IEsptouchResult> result) {
        if (result == null || result.size() == 0) {
            Log.d(TAG, "Received null or empty result in post execute");
            eventSink.endOfStream();
            return;
        }
        Log.d(TAG, "End value" + result.size());
        Log.d(TAG, "End data : " + result.get(result.size()-1).toString());
        IEsptouchResult firstResult = result.get(result.size()-1);
        if (firstResult.isCancelled()) {
            Log.d(TAG, "First result is cancelled");
            eventSink.endOfStream();
            return;
        }
        if (!firstResult.isSuc()){
            Log.d(TAG, "First result is not successful");
            eventSink.endOfStream();
            return;
        }
        for (IEsptouchResult touchResult : result) {
            if(!touchResult.isSuc() || touchResult.isCancelled()) {
                Log.d(TAG, "Touch result is not successful or is cancelled, ignoring");
                continue;
            }
            Map<String, String> sink = new HashMap<>();
            sink.put("bssid", touchResult.getBssid());
            sink.put("ip", touchResult.getInetAddress().getHostAddress());
            eventSink.success(sink);
        }
        eventSink.endOfStream();
    }
}