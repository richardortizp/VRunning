package com.rop.vrunning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.rop.vrunning.helpers.FetchURL;
import com.rop.vrunning.helpers.TaskLoadedCallback;
import com.rop.vrunning.services.LocationService;
import com.rop.vrunning.utils.TrackingUtility;
import com.rop.vrunning.views.ConfiguracionActivity;
import com.rop.vrunning.views.ConfigurationFragment;
import com.rop.vrunning.views.RutaActivity;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, /*OnMapReadyCallback,*/ TaskLoadedCallback {

    private GoogleMap mMap;
    private MarkerOptions place1, place2;
    Button getDirection;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        getDirection = findViewById(R.id.btnGetDirection);
        getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // new FetchURL(MainActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(),"driving"),"driving");

                /*Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(
                                new LatLng(21.341827593336323, -101.94351196289062),
                                new LatLng(21.36484992145867, -101.9146728515625),
                                new LatLng(21.39554073009063, -101.865234375),
                                new LatLng(21.41855461083855, -101.84051513671875),
                                new LatLng(21.441564866043823, -101.81991577148438),
                                new LatLng(21.46584953202535, -101.80206298828125),
                                new LatLng(21.48374090716327, -101.76910400390624),
                                new LatLng(21.496519114833735, -101.744384765625),
                                new LatLng(21.525904732559802, -101.72378540039062),
                                new LatLng(21.55400715345142, -101.69082641601561),
                                new LatLng(21.580827113688514, -101.66061401367186),
                                new LatLng(21.616579336740603, -101.64413452148436),
                                new LatLng(21.65998086747517, -101.62628173828125),
                                new LatLng(21.685505083740697, -101.61941528320312),
                                new LatLng(21.700817443805004, -101.61666870117188),
                                new LatLng(21.70847301324597, -101.656494140625)
                        )
                        .width(8)
                        .color(Color.RED));*/
                showConfigurationDialog();
            }

        });

        place1 = new MarkerOptions().position(new LatLng(21.341827593336323, -101.94351196289062)).title("Location 1");
        place2 = new MarkerOptions().position(new LatLng(21.70847301324597, -101.656494140625)).title("Location 2");
        //MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapNearBy);
        // mapFragment.getMapAsync(this);
    }

    void showConfigurationDialog() {
        Intent intent = new Intent(MainActivity.this, RutaActivity.class);
        intent.putExtra("currentLocation","21.70847301324597,-101.656494140625");
        startActivity(intent);
    }
    void startService() {
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            requestPermissions();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        // EasyPermissions handles the request result
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,this);
    }
    private void requestPermissions() {
        TrackingUtility trackingUtility = new TrackingUtility();

        if (trackingUtility.hasLocationPermissions(this.getApplicationContext())){
            startService();
            Log.d("MyLog","Ya hay permisos se inicia el servicio");
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                    this,
                    "Necesitas aceptar los permisos de ubicacion",
                    0,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "Necesitas aceptar los permisos de ubicacion",
                    0,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
    }
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startService();
        Log.d("MyLog","Permisos concedidos y servicio iniciado");
    }

    /*@Override
    public void onMapReady(GoogleMap googleMap) {
        // mMap = googleMap;
        Log.d("myLog","Added Markers");
        // mMap.addMarker(place1); // agregar marcador
        // mMap.addMarker(place2); // agregar marcador
    }*/

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        Polyline line = mMap.addPolyline(new PolylineOptions().add(new LatLng(21.366129, -101.938030), new LatLng(21.231142, -101.783155), new LatLng(21.118068, -101.682150)).width(5).color(Color.GREEN));
    }
}