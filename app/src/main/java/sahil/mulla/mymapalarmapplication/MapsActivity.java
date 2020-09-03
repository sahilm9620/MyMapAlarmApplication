package sahil.mulla.mymapalarmapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    final static int REQUEST_CODE = 1;
    Circle circle;
    LatLng location;
    public LocationManager lm; // location manager LBS // gives latitude and longitude
    double alarm_location_latitude = 0;
    double alarm_location_longitutde = 0;
    double current_location_latitude = 0;
    double current_location_longitutde = 0;
    boolean state = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //current_location_latitude = 32.361114 ;
        //current_location_longitutde = 74.207883 ;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                current_location_latitude=latLng.latitude;
                current_location_longitutde=latLng.longitude;
                location = new LatLng(current_location_latitude, current_location_longitutde);
                mMap.addMarker(new MarkerOptions().position(location).icon(bitmapDescriptorFromVector(getApplicationContext())).title("Plant Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(20.0f));
            }
        });
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getMyLocation();
        location = new LatLng(current_location_latitude, current_location_longitutde);


        mMap.addMarker(new MarkerOptions().position(location).icon(bitmapDescriptorFromVector(getApplicationContext())).title("Plant Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20.0f));  // zoom in
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context applicationContext) {
        Drawable vectorDrawable = ContextCompat.getDrawable(applicationContext, R.drawable.logo);
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        }
        Bitmap bitmap = null;
        if (vectorDrawable != null)
        {
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = null;
        if (bitmap != null) {
            canvas = new Canvas(bitmap);
        }
        assert canvas != null;
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // update the current location of user
    public void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            Location loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null) {
                current_location_latitude = loc.getLatitude();
            }
            if (loc != null) {
                current_location_longitutde = loc.getLongitude();
            }

            // Toast.makeText(getApplicationContext(),current_location_latitude+" , "+ current_location_longitutde , Toast.LENGTH_SHORT).show();
        }
    }

    // Checks whether user is inside of circle or not
    public boolean IsInCircle() {
        float[] distance = {0, 0, 0};
        Location.distanceBetween(current_location_latitude, current_location_longitutde,
                circle.getCenter().latitude, circle.getCenter().longitude, distance);
        return !(distance[0] > circle.getRadius());
    }

    //------------ Sends user to Set Alarm
    public void addAlaram(View v) {
        //getMyLocation();
        Intent i = new Intent(this, MyAlarmActivity.class);
        i.putExtra("longitude", current_location_longitutde);
        i.putExtra("latitude", current_location_latitude);
        startActivityForResult(i, REQUEST_CODE);
    }


    //-----------After Alarm Set ---------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data.hasExtra("alarm_location_latitude") && data.hasExtra("alarm_location_longitude")) {
                state = true;
                alarm_location_latitude = Objects.requireNonNull(data.getExtras()).getDouble("alarm_location_latitude");
                alarm_location_longitutde = data.getExtras().getDouble("alarm_location_longitude");

                location = new LatLng(alarm_location_latitude, alarm_location_longitutde);
                mMap.addMarker(new MarkerOptions().position(location).title("Alarm Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                // Add a circle of radius 50 meter
                circle = mMap.addCircle(new CircleOptions()
                        .center(new LatLng(alarm_location_latitude, alarm_location_longitutde))
                        .radius(50).strokeColor(Color.RED).fillColor(Color.GREEN));

                //--------------- Check user is in Range or Not after 5 Seconds --------
                final Handler handler = new Handler();
                final int delay = 5000; //milliseconds
                handler.postDelayed(new Runnable() {
                    public void run() {
                        //do something
                        getMyLocation();
                        if (IsInCircle()) {
                            if (state) {
                                Intent intent = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                        getApplicationContext(), 234324243, intent, 0);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                                        + (100), pendingIntent);
                                state = false;
                            }
                        }
                        handler.postDelayed(this, delay);
                    }
                }, delay);
            }
        }

    }

}
