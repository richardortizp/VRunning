package com.rop.vrunning.services;

import static com.rop.vrunning.utils.Constants.NOTIFICATION_CHANNEL_ID;
import static com.rop.vrunning.utils.Constants.NOTIFICATION_CHANNEL_NAME;
import static com.rop.vrunning.utils.Constants.NOTIFICATION_ID;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.rop.vrunning.MainActivity;
import com.rop.vrunning.R;
import com.rop.vrunning.utils.TrackingUtility;
import com.rop.vrunning.views.RutaActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackingService extends LifecycleService {
    boolean isFirstRun = true;

    LocationCallback mLocationCallback;
    FusedLocationProviderClient mFusedLocationClient;
    public MutableLiveData<Boolean> isTracking = new MutableLiveData<>();
    public MutableLiveData<List<LatLng>> pathPoints = new MutableLiveData<>();

    private void postInitialValues() {
        isTracking.postValue(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        postInitialValues();
        mFusedLocationClient = new FusedLocationProviderClient(this);
        isTracking.observe(this, new androidx.lifecycle.Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.d("Test", aBoolean.toString());
                updateLocationTracking(aBoolean);
            }
        });
    }



    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {

        switch (intent.getAction()) {
            case "ACTION_START_OR_RESUME_SERVICE":{
                if(isFirstRun) {
                    startForegroundService();
                    Log.d("MyLogService", "Start service");
                    isFirstRun = false;
                } else {
                    Log.d("MyLogService", "Resuming service");
                    startForegroundService();
                }
            }
            break;
            case "ACTION_PAUSE_SERVICE":{
                pauseService();
                Log.d("MyLogService", "Pause service");
            }
            break;
            case "ACTION_STOP_SERVICE":{
                Log.d("MyLogService", "Stop service");
            }
            break;
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @SuppressLint("MissingPermission")
    private void updateLocationTracking(boolean isTracking) {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (isTracking) {
                    for (Location location:locationResult.getLocations()) {
                        addPathPoint(location);
                        Log.d("MyLog Location", location.getLatitude() +" " + location.getLongitude());
                    }
                }
            }
        };

        if (isTracking) {
            if (new TrackingUtility().hasLocationPermissions(this)) {
                LocationRequest request = new LocationRequest();
                request.setInterval(10);
                request.setFastestInterval(10);
                request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                mFusedLocationClient.requestLocationUpdates(request, mLocationCallback, Looper.myLooper());
            }
            else {
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            }
        }
    }

    private void pauseService() {
        isTracking.postValue(false);
    }

    private void addPathPoint(@Nullable Location location) {
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        List<LatLng> path = pathPoints.getValue();
        path.add(pos);
        pathPoints.postValue(path);
        Log.d("MyLog",pathPoints.getValue().toString());
    }

    private void addEmptyPolyLine() {
        pathPoints.setValue(new ArrayList<>());
    }

    private void startForegroundService() {
        addEmptyPolyLine();
        isTracking.postValue(true);

        NotificationManager notificationManager;
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createNotificationChannel(notificationManager);
        }

        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.common_full_open_on_phone)
                .setContentTitle("V Running")
                .setContentText("00:00:00")
                .setContentIntent(getMainActivityPendingIntent());

        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private PendingIntent getMainActivityPendingIntent() {
        Intent intent = new Intent(this, RutaActivity.class);
        intent.setAction("ACTION_SHOW_TRACKING_FRAGMENT");
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel channel;
        channel = new NotificationChannel(
          NOTIFICATION_CHANNEL_ID,
          NOTIFICATION_CHANNEL_NAME,
          NotificationManager.IMPORTANCE_LOW
        );
        notificationManager.createNotificationChannel(channel);
    }

}
