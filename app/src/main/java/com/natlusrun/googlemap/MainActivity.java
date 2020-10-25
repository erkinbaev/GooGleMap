package com.natlusrun.googlemap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.natlusrun.googlemap.preferences.PreferenceUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, View.OnClickListener {

    private final static int REQUEST_CODE = 1;

    private GoogleMap mMap;
    private Button hybridMapBtn, createPolygonBtn;
    private ArrayList<LatLng> coordinates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkSettings();
        initMap();
        initViews();
    }

    private void checkSettings() {
        if (coordinates != null && getSharedPreferences(PreferenceUtils.PREFS_KEY, Context.MODE_PRIVATE) != null) {
            coordinates = (PreferenceUtils.getLocation());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        CameraPosition cameraPosition = CameraPosition
                .builder()
                .target(PreferenceUtils.mLatLng).zoom(12.95f).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        addPolygons();
        mMap.setOnMapClickListener(this);

    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        coordinates.add(latLng);
        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng);
        marker.title("Marker");
        marker.draggable(true);
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng).radius(50)

                .strokeColor(Color.RED).strokeWidth(5f);
        mMap.addCircle(circleOptions);

        marker.icon(bitmapDescriptorFromVector(this, R.drawable.ic_baseline_add_location_24));
        mMap.addMarker(marker);
        Toast.makeText(this, ""+latLng, Toast.LENGTH_SHORT).show();
        //coordinates.add(latLng);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.btn_create_polygon:
                coordinates.remove(0);
                addPolygons();
                PreferenceUtils.savePolygon(coordinates);
                Toast.makeText(this, "Polygon saved!", Toast.LENGTH_SHORT).show();
                Log.e("TAG", "onClickPolygon: " + coordinates.toString());
                break;
        }


    }

    private void addPolygons() {
        if (coordinates != null) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.strokeWidth(5f);
            polygonOptions.strokeColor(Color.BLUE);
            for (LatLng latLng : coordinates) {
                polygonOptions.add(latLng);
            }
            mMap.addPolygon(polygonOptions);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void initViews() {
        hybridMapBtn = findViewById(R.id.btn_hybrid_map);
        createPolygonBtn = findViewById(R.id.btn_create_polygon);
        hybridMapBtn.setOnClickListener(this);
        createPolygonBtn.setOnClickListener(this);
    }
}