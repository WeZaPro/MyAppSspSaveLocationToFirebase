package example.com.savelocationtofirebase;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    String myAddress = "No identified address";
    Double myLatitude = 0.0;
    Double myLongitude = 0.0;

    // test Firebase
    DatabaseReference locations;
    FirebaseDatabase firebaseDatabase;
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // test Firebase
        locations = FirebaseDatabase.getInstance().getReference("locations");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();

                //Get full address
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    String fullAddress = addresses.get(0).getAddressLine(0);
                    myAddress = fullAddress;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Toast.makeText(MapActivity.this, myAddress, Toast.LENGTH_LONG).show();

                myLatitude = latLng.latitude;
                myLongitude = latLng.longitude;
                mMap.addMarker(new MarkerOptions().position(latLng).title(myAddress));

                // Test Firebase
                //displayLocation();
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(userLocation).title("Current Location  : " + location.getLatitude()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f));
                    Toast.makeText(MapActivity.this, "Lat : "
                            + location.getLatitude() + "\n"
                            + "Long : " + location.getLongitude(), Toast.LENGTH_SHORT).show();

                    // save to Firebase here

                    String uploadID = locations.push().getKey();
                    MyModel myModel = new MyModel(uploadID,"wee",location.getLatitude(),location.getLongitude());
                    locations.child("Test").setValue(myModel);


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

        //Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2f, locationListener);
            mMap.clear();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 17f));
                mMap.addMarker(new MarkerOptions().position(lastUserLocation).title("Current Location 123"));

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2f, locationListener);
                mMap.clear();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 17f));
                    mMap.addMarker(new MarkerOptions().position(lastUserLocation).title("Current Location"));

                    // Test Firebase
                    /*displayLocation();*/
                }
            } else {
                Intent intent = new Intent(MapActivity.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(MapActivity.this, "Please grant permission to continue", Toast.LENGTH_LONG).show();
            }

        }

    }

    // Test Firebase
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;

        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            // save location to firebase **********
            /*locations.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new Tracking(FirebaseAuth.getInstance().getCurrentUser().getEmail(),FirebaseAuth.getInstance().getCurrentUser().getUid(),String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude())));*/

            String uploadID = locations.push().getKey();

            locations.child(uploadID)
                    .setValue(new MyModel(uploadID, "Taweesak", mLastLocation.getLongitude()
                            , mLastLocation.getLongitude()
                    ));
            Toast.makeText(this, "save to firebase", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Couldn't get the location", Toast.LENGTH_SHORT).show();

            /*Log.d("TEST","could not get the location");*/
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        Intent myIntent = new Intent(MapActivity.this, MainActivity.class);

        if (myLatitude == 0.0 || myLongitude == 0.0 || myAddress.equals("No identified address")) {
            Toast.makeText(this, "Please choose different location", Toast.LENGTH_LONG).show();
        }

        myIntent.putExtra("map_latitude", myLatitude.toString());
        myIntent.putExtra("map_longitude", myLongitude.toString());
        myIntent.putExtra("map_address", myAddress);
        startActivity(myIntent);
    }
}
