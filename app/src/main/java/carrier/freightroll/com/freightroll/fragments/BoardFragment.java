package carrier.freightroll.com.freightroll.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.activities.PickupActivity;
import carrier.freightroll.com.freightroll.api.APIInterface;
import carrier.freightroll.com.freightroll.api.APIManager;
import carrier.freightroll.com.freightroll.app.EndPoints;
import carrier.freightroll.com.freightroll.helpers.PreferenceManager;
import carrier.freightroll.com.freightroll.models.DISTANCE;
import carrier.freightroll.com.freightroll.models.TRUCKTYPE;
import carrier.freightroll.com.freightroll.models.TruckModel;


/**
 * A simple {@link Fragment} subclass.
 */
public class BoardFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private static final float MI_TO_METER = 1609.34f;
    private MapView _mapView;
    private GoogleMap _gmap;
    private LinearLayout _btnDistance;
    private RelativeLayout _btnTruckType;
    private LinearLayout _btnPosition;
    private LinearLayout _btnDate;
    private Dialog _distanceDialog;
    private Dialog _truckDialog;
    private DISTANCE _eDistance;
    private TRUCKTYPE _eTruckType;
    private int _iYear;
    private int _iMonth;
    private int _iDay;
    private TextView _txtDate;
    private TextView _txtPosition;
    private String _strPosition;
    private LatLng _currentPosition;
    private Switch _btn_pickups;
    private List<TruckModel> _trucks;

    public BoardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_board, container, false);

        _btnDistance = rootView.findViewById(R.id.ly_distanceLevel);
        _btnDistance.setOnClickListener(this);

        _btnTruckType = rootView.findViewById(R.id.rl_trucktype);
        _btnTruckType.setOnClickListener(this);

        _btnPosition = rootView.findViewById(R.id.ly_position);
        _btnPosition.setOnClickListener(this);

        _txtPosition = rootView.findViewById(R.id.txt_position);

        _btnDate = rootView.findViewById(R.id.ly_date);
        _btnDate.setOnClickListener(this);

        _txtDate = rootView.findViewById(R.id.txt_date);

        String date = PreferenceManager.getDate(this.getActivity());
        if (date == null) {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            _iYear = cal.get(Calendar.YEAR);
            _iMonth = cal.get(Calendar.MONTH);
            _iDay = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            String[] values = date.split("-");
            try {
                _iYear = Integer.parseInt(values[0]);
                _iMonth = Integer.parseInt(values[1]);
                _iDay = Integer.parseInt(values[2]);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }
        showDate();

        _eDistance = DISTANCE.values()[PreferenceManager.getDistance(getActivity())];
        setDistanceText(rootView);

        _eTruckType = TRUCKTYPE.values()[PreferenceManager.getTruck(getActivity())];
        setTruckTypeText(rootView);

        _mapView = rootView.findViewById(R.id.mapView);
        _mapView.onCreate(savedInstanceState);

        _mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        _mapView.getMapAsync(this);

        _btn_pickups = (Switch) rootView.findViewById(R.id.switch_pickups);
        _btn_pickups.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeTrucksState(isChecked);
            }
        });
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _gmap = googleMap;
        _gmap.setMinZoomPreference(3);
        LatLng ny = new LatLng(41.850033, -92.0500523);
        _gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));

        addMarker();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        _mapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        setPosition();
        addMarker();
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

    @Override
    public void onClick(View v) {
        if (v == _btnDistance) {
            showDistanceDialog();
        } else if (v == _btnTruckType) {
            showTruckDialog();
        } else if (v == _btnPosition) {
            goToPickupActivity();
        } else if (v == _btnDate) {
            showDateDialog();
        }
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            setDate(selectedYear, selectedMonth, selectedDay);
        }
    };

    public void showDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.set(_iYear, _iMonth, _iDay, 0, 0, 0);
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
        _txtDate.setText(df.format(cal.getTime()));
    }

    public void showDistanceDialog() {
        _distanceDialog = new Dialog(getActivity());
        _distanceDialog.setContentView(R.layout.distance_layout);
        _distanceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _distanceDialog.setTitle("Custom Alert Dialog");
//        _distanceDialog.setCancelable(false);
        _distanceDialog.show();
        _distanceDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                setDistanceText(getView());
                PreferenceManager.setDistance(getActivity(), _eDistance.getValue());
            }
        });

        Button btn_cancel = _distanceDialog.findViewById(R.id.btn_cancel);
        Button btn_50 = _distanceDialog.findViewById(R.id.btn_show_all);
        Button btn_100 = _distanceDialog.findViewById(R.id.btn_flatbed);
        Button btn_150 = _distanceDialog.findViewById(R.id.btn_dry_van);
        Button btn_200 = _distanceDialog.findViewById(R.id.btn_step_deck);

        switch (_eDistance) {
            case D_50:
                btn_50.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case D_100:
                btn_100.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case D_150:
                btn_150.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case D_200:
                btn_200.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            default:
                break;
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _distanceDialog.dismiss();
            }
        });

        btn_50.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDistance(DISTANCE.D_50);
            }
        });

        btn_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDistance(DISTANCE.D_100);
            }
        });

        btn_150.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDistance(DISTANCE.D_150);
            }
        });

        btn_200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDistance(DISTANCE.D_200);
            }
        });
    }

    public void showTruckDialog() {
        _truckDialog = new Dialog(getActivity());
        _truckDialog.setContentView(R.layout.truck_layout);
        _truckDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _truckDialog.setTitle("Custom Alert Dialog");
//        _truckDialog.setCancelable(false);
        _truckDialog.show();
        _truckDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                setTruckTypeText(getView());
                PreferenceManager.setTruck(getActivity(), _eTruckType.getValue());
            }
        });

        Button btn_cancel = _truckDialog.findViewById(R.id.btn_cancel);
        Button btn_show_all = _truckDialog.findViewById(R.id.btn_show_all);
        Button btn_flatbed = _truckDialog.findViewById(R.id.btn_flatbed);
        Button btn_dry_van = _truckDialog.findViewById(R.id.btn_dry_van);
        Button btn_step_deck = _truckDialog.findViewById(R.id.btn_step_deck);
        Button btn_ltl = _truckDialog.findViewById(R.id.btn_ltl);

        switch (_eTruckType) {
            case SHOW_ALL:
                btn_show_all.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case FLATBED:
                btn_flatbed.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case DRY_VAN:
                btn_dry_van.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case STEP_DECK:
                btn_step_deck.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case LTL:
                btn_ltl.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            default:
                break;
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _truckDialog.dismiss();
            }
        });

        btn_show_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTruckType(TRUCKTYPE.SHOW_ALL);
            }
        });

        btn_flatbed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTruckType(TRUCKTYPE.FLATBED);
            }
        });

        btn_dry_van.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTruckType(TRUCKTYPE.DRY_VAN);
            }
        });

        btn_step_deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTruckType(TRUCKTYPE.STEP_DECK);
            }
        });

        btn_ltl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTruckType(TRUCKTYPE.LTL);
            }
        });
    }

    public void setDate(int year, int month, int day) {
        _iYear = year;
        _iMonth = month;
        _iDay = day;
        showDate();
        String date = String.valueOf(_iYear) + "-" + String.valueOf(_iMonth) + "-" + String.valueOf(_iDay);
        PreferenceManager.setDate(getActivity(), date);
    }

    public void setDistance(DISTANCE d) {
        _eDistance = d;
        addMarker();
        _distanceDialog.dismiss();
    }

    public void setTruckType(TRUCKTYPE t) {
        _eTruckType = t;
        _truckDialog.dismiss();
    }

    public void showDateDialog() {
        DatePickerDialog dp = new DatePickerDialog(this.getActivity(), AlertDialog.THEME_HOLO_LIGHT, datePickerListener, _iYear, _iMonth, _iDay);
        dp.setInverseBackgroundForced(true);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TextView tv = new TextView(getActivity());
        tv.setLayoutParams(lp);
        tv.setPadding(10, 24, 10, 24);
        tv.setGravity(Gravity.CENTER);
        tv.setText(getResources().getString(R.string.bs_select_pickup_date));
        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        dp.setCustomTitle(tv);
        dp.show();
    }

    public void setDistanceText(View v) {
        TextView txtView = v.findViewById(R.id.txtDistance);
        switch (_eDistance) {
            case D_50:
                txtView.setText(getResources().getString(R.string.bs_50_mi));
                break;
            case D_100:
                txtView.setText(getResources().getString(R.string.bs_100_mi));
                break;
            case D_150:
                txtView.setText(getResources().getString(R.string.bs_150_mi));
                break;
            case D_200:
                txtView.setText(getResources().getString(R.string.bs_200_mi));
                break;
            default:
                break;
        }
    }

    public void setTruckTypeText(View v) {
        TextView txtView = v.findViewById(R.id.txtTruckType);
        switch (_eTruckType) {
            case SHOW_ALL:
                txtView.setText(getResources().getString(R.string.bs_text_showall));
                break;
            case FLATBED:
                txtView.setText(getResources().getString(R.string.bs_text_flatbed));
                break;
            case STEP_DECK:
                txtView.setText(getResources().getString(R.string.bs_text_stepdeck));
                break;
            case DRY_VAN:
                txtView.setText(getResources().getString(R.string.bs_text_dryvan));
                break;
            case LTL:
                txtView.setText(getResources().getString(R.string.bs_text_ltl));
                break;
            default:
                break;
        }
    }

    public void goToPickupActivity() {
        Intent intent = new Intent(getActivity(), PickupActivity.class);
        intent.putExtra("cur_lat", _currentPosition == null ? null : _currentPosition.latitude);
        intent.putExtra("cur_lng", _currentPosition == null ? null : _currentPosition.longitude);
        intent.putExtra("label", _strPosition);
        this.startActivity(intent);
    }

    public void setPosition() {
        float lat = PreferenceManager.getPositionLat(getActivity());
        float lng = PreferenceManager.getPositionLng(getActivity());

        if (lat == -1000) {
            _currentPosition = null;
        } else {
            _currentPosition = new LatLng(lat, lng);
        }

        _strPosition = PreferenceManager.getPositionLabel(getActivity());
        if (_strPosition != null) {
            _txtPosition.setText(_strPosition);
        }
    }

    public void addMarker() {
        if (_gmap == null) return;
        _gmap.clear();

        if (_currentPosition == null) return;

        CircleOptions optCircle = new CircleOptions();
        optCircle.center(_currentPosition);
        if (_eDistance == DISTANCE.D_200) {
            optCircle.radius(200 * MI_TO_METER);
        } else if (_eDistance == DISTANCE.D_150) {
            optCircle.radius(150 * MI_TO_METER);
        } else if (_eDistance == DISTANCE.D_100) {
            optCircle.radius(100 * MI_TO_METER);
        } else if (_eDistance == DISTANCE.D_50) {
            optCircle.radius(50 * MI_TO_METER);
        }
        optCircle.fillColor(0x2649B5E8);
        optCircle.strokeWidth(0f);
        Circle circle = _gmap.addCircle(optCircle);

        MarkerOptions optMarker = new MarkerOptions();
        optMarker.anchor(0.5f, 0.5f);
        optMarker.draggable(false);
        optMarker.position(_currentPosition);
        optMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_00));
        _gmap.addMarker(optMarker);

        setCamera();
    }

    public void setCamera() {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(_currentPosition, 8.2f);
        switch (_eDistance) {
            case D_100:
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(_currentPosition, 7.2f);
                break;
            case D_150:
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(_currentPosition, 6.6f);
                break;
            case D_200:
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(_currentPosition, 6.2f);
                break;
        }
        _gmap.animateCamera(cameraUpdate);

    }

    public void changeTrucksState(boolean status) {
        getAllTrucks(status);
    }

    public void getAllTrucks(boolean status) {
        _trucks = new ArrayList<>();

        if (status) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.Theme_AppCompat_DayNight_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Getting Data...");
            progressDialog.show();

            APIManager.get(EndPoints.TRUCK_ALL, null, new APIInterface() {
                @Override
                public void onSuccess(JSONObject response) throws JSONException {
                    progressDialog.dismiss();
//                    onLoginSuccess(response);
                }

                @Override
                public void onSuccess(JSONArray response) throws JSONException {
                    progressDialog.dismiss();
//                    onLoginSuccess(response);
                }

                @Override
                public void onFailure(JSONObject response) throws JSONException {
                    progressDialog.dismiss();
//                    onLoginFailed(response.get("errorString").toString());
                }
            });
        }
    }

}
