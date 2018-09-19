package carrier.freightroll.com.freightroll.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.api.APIInterface;
import carrier.freightroll.com.freightroll.api.APIManager;
import carrier.freightroll.com.freightroll.api.ReadTask;
import carrier.freightroll.com.freightroll.app.EndPoints;
import carrier.freightroll.com.freightroll.helpers.FunctionManager;

public class ShipmentDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private String _strShipmentId;
    private MapView _mapView;
    private GoogleMap _gmap;
    private LatLng _fromPos;
    private LatLng _toPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipment_details);

        Intent intent = getIntent();
        try {
            JSONObject obj = new JSONObject(intent.getStringExtra("info"));
            Log.d("zzz passing object", obj.toString());
            setControls(obj);
            _strShipmentId = obj.getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        _gmap.setMinZoomPreference(3);

        LatLng ny = new LatLng(41.850033, -92.0500523);
        _gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));

        addPins();
        drawLoad();
        setCamera();
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

    public void addPins() {
        _gmap.clear();
        addPin(_fromPos, 0);
        addPin(_toPos, 1);
    }

    public void drawLoad() {
        FunctionManager._gmap = _gmap;
        String url = FunctionManager.getMapsApiDirectionsUrl(_fromPos, _toPos);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);
    }

    public void setCamera() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(_fromPos);
        builder.include(_toPos);
        final LatLngBounds bounds = builder.build();

        _gmap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                _gmap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        });
    }

    public void addPin(LatLng latLng, int index) {
        MarkerOptions optMarker = new MarkerOptions();
        optMarker.anchor(0.22f, 0.91f);
        optMarker.draggable(false);
        optMarker.position(latLng);
        if (index == 0) {
            optMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_01));
        } else {
            optMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_02));
        }
        _gmap.addMarker(optMarker);

    }

    public void setControls(JSONObject obj) throws JSONException {
        JSONObject fromObj = obj.getJSONObject("pickup");
        JSONObject toObj = obj.getJSONObject("dropoff");

        _fromPos = new LatLng(fromObj.getDouble("lat"), fromObj.getDouble("lon"));
        _toPos = new LatLng(toObj.getDouble("lat"), toObj.getDouble("lon"));

        if (obj.getString("status").equalsIgnoreCase("awaiting_confirmation") ) {
            Button btnAccept = findViewById(R.id.btn_accept);
            btnAccept.setEnabled(false);
        }

        TextView txtRate = findViewById(R.id.txt_rate);
        txtRate.setText(FunctionManager.convertCurrency(obj.getDouble("rate")));

        TextView txtFromPlace = findViewById(R.id.txt_from_place);
        txtFromPlace.setText(FunctionManager.combineCityAndState(fromObj.getString("city"), fromObj.getString("state")));

        TextView txtToPlace = findViewById(R.id.txt_to_place);
        txtToPlace.setText(FunctionManager.combineCityAndState(toObj.getString("city"), toObj.getString("state")));

        TextView txtFromDate = findViewById(R.id.txt_from_date);
        txtFromDate.setText(FunctionManager.combineStartAndEndDate(fromObj.getString("date"), fromObj.getString("latest_date")));

        TextView txtToDate = findViewById(R.id.txt_to_date);
        txtToDate.setText(FunctionManager.combineStartAndEndDate(toObj.getString("date"), toObj.getString("latest_date")));

        TextView txtDistance = findViewById(R.id.txt_distance);
        txtDistance.setText(obj.getString("distance") + " mi.");

        TextView txtTruckType = findViewById(R.id.txt_truck_type);
        txtTruckType.setText(FunctionManager.getStringTruckType(this, FunctionManager.getEnumTruckType(obj.getString("truck_type"))));

        TextView txtWeight = findViewById(R.id.txt_weight);
        txtWeight.setText(obj.getString("weight") + "lbs");

        TextView txtFromAddress = findViewById(R.id.txt_from_address);
        txtFromAddress.setText(FunctionManager.combineCityAndState(fromObj.getString("city"), fromObj.getString("state")) + " " + fromObj.getString("zip_code"));

        TextView txtToAddress = findViewById(R.id.txt_to_address);
        txtToAddress.setText(FunctionManager.combineCityAndState(toObj.getString("city"), toObj.getString("state")) + " " + toObj.getString("zip_code"));

        TextView txtFromAppointment = findViewById(R.id.txt_from_appointment);
        txtFromAppointment.setText(fromObj.getBoolean("appointment_needed") ? getString(R.string.sd_yes) : getString(R.string.sd_no));

        TextView txtToAppointment = findViewById(R.id.txt_to_appointment);
        txtToAppointment.setText(toObj.getBoolean("appointment_needed") ? getString(R.string.sd_yes) : getString(R.string.sd_no));

        TextView txtFromDateRange = findViewById(R.id.txt_from_date_range);
        txtFromDateRange.setText(FunctionManager.combineStartAndEndDate(fromObj.getString("date"), fromObj.getString("latest_date")));

        TextView txtToDateRange = findViewById(R.id.txt_to_date_range);
        txtToDateRange.setText(FunctionManager.combineStartAndEndDate(toObj.getString("date"), toObj.getString("latest_date")));

        TextView txtFromShippingHours = findViewById(R.id.txt_from_shipping_hours);
        txtFromShippingHours.setText(FunctionManager.combineStartAndEndHours(fromObj.getString("date"), fromObj.getString("latest_date")));

        TextView txtToShippingHours = findViewById(R.id.txt_to_shipping_hours);
        txtToShippingHours.setText(FunctionManager.combineStartAndEndHours(toObj.getString("date"), toObj.getString("latest_date")));

        TextView txtFromPickupInstructions = findViewById(R.id.txt_from_pickup_instructions);
        txtFromPickupInstructions.setText(fromObj.getString("other_instructions"));

        TextView txtToPickupInstructions = findViewById(R.id.txt_to_pickup_instructions);
        txtToPickupInstructions.setText(toObj.getString("other_instructions"));
    }

    public void accept() {
        final Button btnAccept = findViewById(R.id.btn_accept);
        btnAccept.setEnabled(false);

        String url = EndPoints.ACCEPT_SHIPMENT.replace(":id", _strShipmentId);

        final ProgressDialog progressDialog = new ProgressDialog(ShipmentDetailsActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Accepting...");
        progressDialog.show();

        APIManager.post(url, null, new APIInterface() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                progressDialog.dismiss();
                Log.d("zzz success", response.toString());
                Toast.makeText(getBaseContext(), getString(R.string.sd_shipment_accepted), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onSuccess(JSONArray response) throws JSONException {
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(JSONObject response) throws JSONException {
                progressDialog.dismiss();
                btnAccept.setEnabled(true);
                Toast.makeText(getBaseContext(), response.get("message").toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void confirmAction(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.sd_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                accept();
            }
        });
        builder.setNegativeButton(R.string.sd_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setMessage(getString(R.string.sd_accept_load));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void goBack(View v) {
        finish();
    }
}
