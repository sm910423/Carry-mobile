package carrier.freightroll.com.freightroll.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.helpers.PreferenceManager;

public class PickupActivity extends AppCompatActivity implements OnMapReadyCallback {
    private float _cur_lat = -1000;
    private float _cur_lng = -1000;
    private String _cur_label;
    private MapView _mapView;
    private GoogleMap _gmap;
    private SearchView _searchView;
    private boolean _isDropped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pickup);

        Intent intent = getIntent();
        _cur_label = intent.getStringExtra("label");
        _cur_lat = (float) intent.getDoubleExtra("cur_lat", -1000);
        _cur_lng = (float) intent.getDoubleExtra("cur_lng", -1000);
        showPlace();

        _searchView = findViewById(R.id.srch_place);
        _searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return searchAddress(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        LinearLayout btnSearch = findViewById(R.id.ly_place);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editableSearchBox();
            }
        });

        _mapView = findViewById(R.id.mapView);
        _mapView.onCreate(savedInstanceState);

        _mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        _mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _gmap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            _gmap.setMyLocationEnabled(true);
        }
        _gmap.setMinZoomPreference(3);
        _gmap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                clickedMap(latLng);
            }
        });

        LatLng ny = new LatLng(41.850033, -92.0500523);
        _gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));

        _gmap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                try {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                    if (location == null) {
                        return false;
                    }
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 3);
                    _gmap.animateCamera(cameraUpdate);

                    clickedMap(new LatLng(latitude, longitude));
                    return true;
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        _gmap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener() {
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                Log.d("zzz ", "2");
            }
        });

        if (_cur_lat != -1000) {
            addMarker(new LatLng(_cur_lat, _cur_lng));
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        _mapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        _mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        _mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _mapView.onDestroy();
    }

    public boolean searchAddress(String query) {
        if (setPlaceFromString(query)) {
            _isDropped = true;
            LatLng latLng = new LatLng(_cur_lat, _cur_lng);
            addMarker(latLng);
            showPlace();
        }

        return false;
    }

    public void clickedMap(LatLng latLng) {
        _isDropped = true;
        _cur_lat = (float)latLng.latitude;
        _cur_lng = (float)latLng.longitude;

        addMarker(latLng);
        setPlaceFromPosition(latLng);
        showPlace();
    }

    public void addMarker(LatLng latLng) {
        _gmap.clear();
        MarkerOptions optMarker = new MarkerOptions();
        optMarker.anchor(0.22f, 0.91f);
        optMarker.draggable(false);
        optMarker.position(latLng);
        optMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_01));
        _gmap.addMarker(optMarker);
    }

    public boolean setPlaceFromString(String query) {
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(query, 1);
            if (addresses.size() > 0) {
//            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                _cur_lat = (float) addresses.get(0).getLatitude();
                _cur_lng = (float) addresses.get(0).getLongitude();

                if (city != null) {
                    _cur_label = city + "/" + state;
                } else {
                    _cur_label = state;
                }
                return true;
            } else {
                _cur_label = "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setPlaceFromPosition(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
//            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                if (city != null) {
                    _cur_label = city + "/" + state;
                } else {
                    _cur_label = state;
                }
            } else {
                _cur_label = "";
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showPlace() {
        if (_cur_label != null) {
            TextView txtPlace = findViewById(R.id.txt_place);
            txtPlace.setText(_cur_label);
        }
    }

    public void editableSearchBox() {
        _searchView.setIconified(false);
    }

    public void goBack(View v) {
        if (_isDropped) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.pk_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    save(getWindow().getDecorView());
                }
            });
            builder.setNegativeButton(R.string.pk_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setMessage(getString(R.string.pk_comment_save));
            AlertDialog dialog = builder.create();
//            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialog) {
//
//                }
//            });
            dialog.show();
        } else {
            finish();
        }
    }

    public void save(View v) {
        PreferenceManager.setPositionLat(PickupActivity.this, _cur_lat);
        PreferenceManager.setPositionLng(PickupActivity.this, _cur_lng);
        PreferenceManager.setPositionLabel(PickupActivity.this, _cur_label);
        finish();
    }
}
