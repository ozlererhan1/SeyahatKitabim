package com.example.erhan.seyahatkitabm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.security.spec.ECField;
import java.util.List;
import java.util.Locale;

// Adresleri almak için onmaplongclicklistener ı ekledik
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
static SQLiteDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if (info.matches("new")) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("notfirsttime", MODE_PRIVATE);
                    boolean checked = sharedPreferences.getBoolean("notfirsttime", false);

                    if (checked == false) {
                        LatLng userlocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation, 15));
                        sharedPreferences.edit().putBoolean("notfirsttime", true);
                    }


                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1
                    );
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    mMap.clear();
                    Location lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    LatLng lastuserlocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastuserlocation, 15));

                }
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                mMap.clear();
                Location lastlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                LatLng lastuserlocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastuserlocation, 15));

            }
        }else {
            mMap.clear();
            mMap.clear();
            int position = intent.getIntExtra("position",0);
            LatLng location = new LatLng(MainActivity.locations.get(position).latitude,MainActivity.locations.get(position).longitude);
            String placeName = MainActivity.names.get(position);

            mMap.addMarker(new MarkerOptions().title(placeName).position(location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0){
            if (requestCode==1){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location lastlocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastlocation!=null){
                        LatLng lastuserlocation=new LatLng(lastlocation.getLatitude(),lastlocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastuserlocation,15));
                    }

                }
            }
        }

    }



    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";
        try {
            List<Address>addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (addressList!=null&&addressList.size()>0){
                if (addressList.get(0).getThoroughfare()!=null){
                    address+=addressList.get(0).getThoroughfare();
                    if (addressList.get(0).getSubThoroughfare()!=null){
                        address+=addressList.get(0).getSubThoroughfare();
                    }
                }else {
                    address="New Place";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMap.addMarker(new MarkerOptions().title(address).position(latLng));
        Toast.makeText(getApplicationContext(),"New Place OK!",Toast.LENGTH_LONG).show();

        MainActivity.names.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();



        try {
            Double l1=latLng.latitude;
            Double l2=latLng.longitude;
            String coord1=l1.toString();
            String coord2=l2.toString();

            database=this.openOrCreateDatabase("Places",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS places(name VARCHAR,latitude VARCHAR,longitude VARCHAR)");
            String toCompile="INSERT INTO places (name,latitude,longitude) VALUES(?,?,?)";
            SQLiteStatement statement=database.compileStatement(toCompile);
            statement.bindString(1,address);
            statement.bindString(2,coord1);
            statement.bindString(3,coord2);

            statement.execute();



        }catch (Exception e){
            e.printStackTrace();
        }



    }
}
