package com.rop.vrunning.views;

import static com.rop.vrunning.utils.Constants.MAP_ZOOM;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rop.vrunning.R;
import com.rop.vrunning.services.TrackingService;

import java.util.ArrayList;
import java.util.List;

public class RutaActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap map;
    MapView mapView;
    LatLng initLatLng;

    private Boolean isTracking = false;
    private List<LatLng> pathPoints = new ArrayList<LatLng>();
    private MarkerOptions place1, place2;
    Button btnToggle;

    TrackingService trackingService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta);
        btnToggle = findViewById(R.id.btnToggle);
        trackingService = new TrackingService();

        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleRun();
            }
        });

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String latlng = extras.getString("currentLocation");
            double lat = Double.parseDouble(latlng.split(",")[0]);
            double lng = Double.parseDouble(latlng.split(",")[1]);
            initLatLng = new LatLng(lat,lng);
        }

        subscribeToObservers();
    }

    private void toggleRun() {
        if(isTracking) {
            btnToggle.setText("START");
            sendCommandToService("ACTION_PAUSE_SERVICE");
        } else {
            btnToggle.setText("STOP");
            sendCommandToService("ACTION_START_OR_RESUME_SERVICE");
        }
    }

    private void sendCommandToService(String action){
        Intent intent = new Intent(this, trackingService.getClass());
        intent.setAction(action);
        startService(intent);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (initLatLng != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initLatLng, 12));
            // place1 = new MarkerOptions().position(initLatLng).title("Punto de partida").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            // map.addMarker(place1);
            addAllPolylines();
        }
    }

    private void subscribeToObservers(){
        Log.d("Test puntos","Subscribir a los observers");
        trackingService.isTracking.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        trackingService.pathPoints.observe(this, new Observer<List<LatLng>>() {
            @Override
            public void onChanged(List<LatLng> latLongs) {
                pathPoints = latLongs;
                Log.d("Test puntos",pathPoints.toString());
                addLatestPolyline();
                moveCameraToUser();
            }
        });
    }

    private void updateTracking(Boolean isTracking){
        this.isTracking = isTracking;

        if (!isTracking){
            btnToggle.setText("Start");
            // btnFinishRun.visibility = View.VISIBLE;
        } else {
            btnToggle.setText("Stop");
            // btnFinishRun.visibility = View.GONE;
        }
    }

    private void moveCameraToUser(){
        if (!pathPoints.isEmpty() && pathPoints.size() > 0) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(pathPoints.get(pathPoints.size()), MAP_ZOOM));
            map.addMarker(new MarkerOptions().position(pathPoints.get(pathPoints.size())));
        }
    }

    private void addAllPolylines() {
        List<LatLng> polyline = pathPoints;
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.GREEN)
                    .width(8)
                    .addAll(polyline);
            map.addPolyline(polylineOptions);
    }

    private void addLatestPolyline(){
        if (!pathPoints.isEmpty() && pathPoints.size() > 1) {
            LatLng preLastLatLng = pathPoints.get(pathPoints.size()-2);
            LatLng lastLatLng = pathPoints.get(pathPoints.size());
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(Color.GREEN)
                    .width(9)
                    .add(preLastLatLng)
                    .add(lastLatLng);
            map.addPolyline(polylineOptions);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}