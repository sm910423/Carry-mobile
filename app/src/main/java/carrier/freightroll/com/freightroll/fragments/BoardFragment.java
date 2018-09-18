package carrier.freightroll.com.freightroll.fragments;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.activities.PickupActivity;
import carrier.freightroll.com.freightroll.adapters.ShipmentsAdapter;
import carrier.freightroll.com.freightroll.api.APIInterface;
import carrier.freightroll.com.freightroll.api.APIManager;
import carrier.freightroll.com.freightroll.app.EndPoints;
import carrier.freightroll.com.freightroll.helpers.PreferenceManager;
import carrier.freightroll.com.freightroll.models.DISTANCE;
import carrier.freightroll.com.freightroll.models.SORTOPTION;
import carrier.freightroll.com.freightroll.models.TRUCKTYPE;


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
    private LinearLayout _btnSort;
    private Dialog _distanceDialog;
    private Dialog _truckDialog;
    private Dialog _sortDialog;
    private DISTANCE _eDistance;
    private TRUCKTYPE _eTruckType;
    private SORTOPTION _eSortOption;
    private int _iYear;
    private int _iMonth;
    private int _iDay;
    private TextView _txtDate;
    private TextView _txtPosition;
    private String _strPosition;
    private LatLng _currentPosition;
    private Switch _btn_pickups;
    private JSONArray _trucks;
    private JSONArray _showTrucks;
    private TextView _noResult;
    private RelativeLayout _rl_header_row;
    private RecyclerView _rv_shipments;
    private boolean isFirst = true;
    private ShipmentsAdapter _shipmentsAdapter;
    private List<Marker> _markers;

    public BoardFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_board, container, false);

        _btnDistance = rootView.findViewById(R.id.ly_distanceLevel);
        _btnDistance.setOnClickListener(this);

        _btnTruckType = rootView.findViewById(R.id.rl_trucktype);
        _btnTruckType.setOnClickListener(this);

        _btnSort = rootView.findViewById(R.id.btn_sort);
        _btnSort.setOnClickListener(this);

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

        _eSortOption = SORTOPTION.values()[PreferenceManager.getSortOption(getActivity())];
        setSortOptionText(rootView);

        _noResult = rootView.findViewById(R.id.txt_no_result);
        _rl_header_row = rootView.findViewById(R.id.rl_header_row);
        _rv_shipments = rootView.findViewById(R.id.rv_shipments);
        addNoResultText(rootView);

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

        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isFirst) {
                    isFirst = false;
                    RelativeLayout rl_body = rootView.findViewById(R.id.rl_body);
                    Switch sw = rootView.findViewById(R.id.switch_pickups);
                    int height = rl_body.getHeight() - sw.getHeight() - _noResult.getHeight();
                    _mapView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
                }
            }
        });
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _gmap = googleMap;
        setPermission();
        _gmap.setMinZoomPreference(3);
        LatLng ny = new LatLng(41.850033, -92.0500523);
        _gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));

        drawCircle();
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
        drawCircle();
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
        } else if (v == _btnSort) {
            showSortDialog();
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
        Button btn_50 = _distanceDialog.findViewById(R.id.btn_relevance);
        Button btn_100 = _distanceDialog.findViewById(R.id.btn_rate);
        Button btn_150 = _distanceDialog.findViewById(R.id.btn_distance);
        Button btn_200 = _distanceDialog.findViewById(R.id.btn_pickup_date);

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
                filterByOptions();
                addPins();
                addTruckElements(getView());
            }
        });

        Button btn_cancel = _truckDialog.findViewById(R.id.btn_cancel);
        Button btn_show_all = _truckDialog.findViewById(R.id.btn_relevance);
        Button btn_flatbed = _truckDialog.findViewById(R.id.btn_rate);
        Button btn_dry_van = _truckDialog.findViewById(R.id.btn_distance);
        Button btn_step_deck = _truckDialog.findViewById(R.id.btn_pickup_date);
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

    public void showSortDialog() {
        _sortDialog = new Dialog(getActivity());
        _sortDialog.setContentView(R.layout.sort_layout);
        _sortDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _sortDialog.setTitle("Custom Alert Dialog");
//        _truckDialog.setCancelable(false);
        _sortDialog.show();
        _sortDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                setSortOptionText(getView());
                PreferenceManager.setSortOption(getActivity(), _eSortOption.getValue());
                sortByOptions();
                addTruckElements(getView());
            }
        });

        Button btn_cancel = _sortDialog.findViewById(R.id.btn_cancel);
        Button btn_by_relevance = _sortDialog.findViewById(R.id.btn_relevance);
        Button btn_by_rate = _sortDialog.findViewById(R.id.btn_rate);
        Button btn_by_distance = _sortDialog.findViewById(R.id.btn_distance);
        Button btn_by_pickup_date = _sortDialog.findViewById(R.id.btn_pickup_date);
        Button btn_by_delivery_date = _sortDialog.findViewById(R.id.btn_delivery_date);

        switch (_eSortOption) {
            case RELEVANCE:
                btn_by_relevance.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case RATE:
                btn_by_rate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case DISTANCE:
                btn_by_distance.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case PICKUP_DATE:
                btn_by_pickup_date.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case DELIVERY_DATE:
                btn_by_delivery_date.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            default:
                break;
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _sortDialog.dismiss();
            }
        });

        btn_by_relevance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_currentPosition == null) {
                    Toast.makeText(getContext(), getString(R.string.bs_comment_drop_pin), Toast.LENGTH_LONG).show();
                    _sortDialog.dismiss();
                    return;
                }
                setSortOption(SORTOPTION.RELEVANCE);
            }
        });

        btn_by_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortOption(SORTOPTION.RATE);
            }
        });

        btn_by_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortOption(SORTOPTION.DISTANCE);
            }
        });

        btn_by_pickup_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortOption(SORTOPTION.PICKUP_DATE);
            }
        });

        btn_by_delivery_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSortOption(SORTOPTION.DELIVERY_DATE);
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
        filterByOptions();
        addPins();
        addTruckElements(getView());
    }

    public void setDistance(DISTANCE d) {
        _eDistance = d;
        drawCircle();
        _distanceDialog.dismiss();
    }

    public void setTruckType(TRUCKTYPE t) {
        _eTruckType = t;
        _truckDialog.dismiss();
    }

    public void setSortOption(SORTOPTION s) {
        _eSortOption = s;
        _sortDialog.dismiss();
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

    public void setSortOptionText(View v) {
        TextView txtView = v.findViewById(R.id.txt_sort);
        switch (_eSortOption) {
            case RELEVANCE:
                txtView.setText(getResources().getString(R.string.bs_by_relevance));
                break;
            case RATE:
                txtView.setText(getResources().getString(R.string.bs_by_rate));
                break;
            case DISTANCE:
                txtView.setText(getResources().getString(R.string.bs_by_distance));
                break;
            case PICKUP_DATE:
                txtView.setText(getResources().getString(R.string.bs_by_pickup_date));
                break;
            case DELIVERY_DATE:
                txtView.setText(getResources().getString(R.string.bs_by_delivery_date));
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

    public void drawCircle() {
        if (_gmap == null) return;
        _gmap.clear();

        if (_markers != null && _markers.size() > 0) {
            List<Marker> tempList = new ArrayList<>();
            for (int i = 0; i < _markers.size(); i ++) {
                Marker marker = _markers.get(i);
//                addPin(marker.getPosition());
                tempList.add(addPin(marker.getPosition()));
            }
            _markers = tempList;
        }

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
        _gmap.addCircle(optCircle);

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
        _trucks = new JSONArray();

        if (status) {
            getAllTrucks(status);
        } else {
            removePins();
            addNoResultText(getView());
        }
    }

    public void addNoResultText(View v) {
        LinearLayout container = v.findViewById(R.id.ly_result);
        container.removeAllViews();
        TextView no_result = _noResult;
        container.addView(no_result);
    }

    public void addTruckElements(View v) {
        if (_showTrucks == null) {
            return;
        }

        _shipmentsAdapter = new ShipmentsAdapter(_showTrucks, new ShipmentsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(JSONObject item) {
                Log.d("zzz item clicked", item.toString());
            }
        });
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getContext());
        _rv_shipments.setLayoutManager(lm);
        _rv_shipments.setItemAnimator(new DefaultItemAnimator());
        _rv_shipments.setAdapter(_shipmentsAdapter);

        _shipmentsAdapter.notifyDataSetChanged();

        LinearLayout container = v.findViewById(R.id.ly_result);
        container.removeAllViews();
        RelativeLayout header_row = _rl_header_row;
        container.addView(header_row);
        RecyclerView shipments = _rv_shipments;
        container.addView(shipments);

        TextView txt = v.findViewById(R.id.txt_shipments_count);
        txt.setText(String.valueOf(_showTrucks.length()));
    }

    public void getAllTrucks(boolean status) {
        final ProgressDialog progressDialog = new ProgressDialog(getContext(), R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Getting Data...");
        progressDialog.show();

        APIManager.get(EndPoints.TRUCK_ALL, null, new APIInterface() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess(JSONArray response) throws JSONException {
                _trucks = response;

                Log.d("zzz trucks", _trucks.toString());

                if (_trucks.length() <= 0) {
                    progressDialog.dismiss();
                    return;
                }

                sortByOptions();
//                filterByOptions();
                addPins();

                addTruckElements(getView());

                progressDialog.dismiss();
            }

            @Override
            public void onFailure(JSONObject response) throws JSONException {
                progressDialog.dismiss();
            }
        });
    }

    public void filterByOptions() {
        if (_trucks == null || _trucks.length() == 0) {
            return;
        }

        String searchStr = "";
        switch (_eTruckType) {
            case SHOW_ALL:
                break;
            case FLATBED:
                searchStr = "flatbed";
                break;
            case DRY_VAN:
                searchStr = "dry_van";
                break;
            case STEP_DECK:
                searchStr = "step_deck";
                break;
            case LTL:
                searchStr = "ltl";
                break;
        }

        JSONArray tempArr = new JSONArray();
        for (int i = 0; i < _trucks.length(); i ++) {
            JSONObject tempObj = new JSONObject();
            try {
                tempObj = _trucks.getJSONObject(i);

                if (searchStr == "" || tempObj.getString("truck_type").equalsIgnoreCase(searchStr)) {
                    JSONObject pickup = tempObj.getJSONObject("pickup");
                    if (pickup != null) {
                        String strDate = pickup.getString("date");
                        DateFormat format1 = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US);
                        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                        try {
                            if (format1.parse(_txtDate.getText().toString()).compareTo(format2.parse(strDate)) <= 0) {
                                    tempArr.put(tempObj);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        _showTrucks = tempArr;
    }

    public void sortByOptions() {
        if (_trucks == null || _trucks.length() == 0) {
            return;
        }

        List<JSONObject> tempList = new ArrayList<>();
        switch (_eSortOption) {
            case RELEVANCE:
                tempList = sortByRelevance();
                break;
            case RATE:
                tempList = sortByRate();
                break;
            case DISTANCE:
                tempList = sortByDistance();
                break;
            case PICKUP_DATE:
                tempList = sortByPickupDate();
                break;
            case DELIVERY_DATE:
                tempList = sortByDeliveryDate();
                break;
        }

        _trucks = new JSONArray();
        for (JSONObject obj: tempList) {
            _trucks.put(obj);
        }

        filterByOptions();
    }

    public List<JSONObject> sortByRelevance() {
        List<JSONObject> tempList = new ArrayList<>();
        for (int i = 0; i < _trucks.length(); i ++) {
            try {
                JSONObject obj = _trucks.getJSONObject(i);
                JSONObject objPickup = obj.getJSONObject("pickup");

                if (objPickup == null) continue;

                Location loc1 = new Location("");
                loc1.setLatitude(Double.parseDouble(objPickup.getString("lat")));
                loc1.setLongitude(Double.parseDouble(objPickup.getString("lon")));

                Location loc2 = new Location("");
                loc2.setLatitude(_currentPosition != null ? _currentPosition.latitude : 0);
                loc2.setLongitude(_currentPosition != null ? _currentPosition.longitude : 0);

                double distance = loc1.distanceTo(loc2);
                obj.put("relevance", distance);
                tempList.add(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        MyComparatorByRelevance c = new MyComparatorByRelevance();
        Collections.sort(tempList, c);

        return tempList;
    }

    public List<JSONObject> sortByDistance() {
        List<JSONObject> tempList = new ArrayList<>();
        for (int i = 0; i < _trucks.length(); i ++) {
            try {
                tempList.add(_trucks.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        MyComparatorByDistance c = new MyComparatorByDistance();
        Collections.sort(tempList, c);

        return tempList;
    }

    public List<JSONObject> sortByRate() {
        List<JSONObject> tempList = new ArrayList<>();
        for (int i = 0; i < _trucks.length(); i ++) {
            try {
                tempList.add(_trucks.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        MyComparatorByRate c = new MyComparatorByRate();
        Collections.sort(tempList, c);

        return tempList;
    }

    public List<JSONObject> sortByPickupDate() {
        List<JSONObject> tempList = new ArrayList<>();
        for (int i = 0; i < _trucks.length(); i ++) {
            try {
                tempList.add(_trucks.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        MyComparatorByPickupDate c = new MyComparatorByPickupDate();
        Collections.sort(tempList, c);

        return tempList;
    }

    public List<JSONObject> sortByDeliveryDate() {
        List<JSONObject> tempList = new ArrayList<>();
        for (int i = 0; i < _trucks.length(); i ++) {
            try {
                tempList.add(_trucks.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        MyComparatorByDeliveryDate c = new MyComparatorByDeliveryDate();
        Collections.sort(tempList, c);

        return tempList;
    }

    public void addPins() {
        if (_showTrucks == null) return;

        removePins();

        _markers = new ArrayList<>();
        for (int i = 0; i < _showTrucks.length(); i ++) {
            try {
                JSONObject pickup = _showTrucks.getJSONObject(i).getJSONObject("pickup");
                if (pickup != null) {
                    LatLng pos = new LatLng(pickup.getDouble("lat"), pickup.getDouble("lon"));
                    _markers.add(addPin(pos));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void removePins() {
        if (_markers != null && _markers.size() > 0) {
            for (int i = 0; i < _markers.size(); i ++) {
                Marker marker = _markers.get(i);
                marker.remove();
            }
        }
    }

    public Marker addPin(LatLng latLng) {
        MarkerOptions optMarker = new MarkerOptions();
        optMarker.anchor(0.22f, 0.91f);
        optMarker.draggable(false);
        optMarker.position(latLng);
        optMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_01));
        return _gmap.addMarker(optMarker);
    }

    public void setPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getContext())
                        .setTitle("location")
                        .setMessage("content")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
            }

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                new AlertDialog.Builder(getContext())
                        .setTitle("location")
                        .setMessage("content")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 99);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 99);
            }
        }
    }

    class MyComparatorByRelevance implements Comparator<JSONObject> {
        public int compare(JSONObject a, JSONObject b) {
            double val1, val2;
            try {
                val1 = a.getDouble("relevance");
                val2 = b.getDouble("relevance");
            } catch (JSONException e) {
                val1 = 0;
                val2 = 0;
                e.printStackTrace();
            }
            if (val1 > val2) {
                return 1;
            } else if (val1 < val2) {
                return -1;
            }
            return 0;
        }
    }

    class MyComparatorByRate implements Comparator<JSONObject> {
        public int compare(JSONObject a, JSONObject b) {
            double val1, val2;
            try {
                val1 = a.getDouble("rate");
                val2 = b.getDouble("rate");
            } catch (JSONException e) {
                val1 = 0;
                val2 = 0;
                e.printStackTrace();
            }
            if (val1 > val2) {
                return 1;
            } else if (val1 < val2) {
                return -1;
            }
            return 0;
        }
    }

    class MyComparatorByDistance implements Comparator<JSONObject> {
        public int compare(JSONObject a, JSONObject b) {
            double val1, val2;
            try {
                val1 = a.getInt("distance");
                val2 = b.getInt("distance");
            } catch (JSONException e) {
                val1 = 0;
                val2 = 0;
                e.printStackTrace();
            }
            if (val1 > val2) {
                return 1;
            } else if (val1 < val2) {
                return -1;
            }
            return 0;
        }
    }

    class MyComparatorByPickupDate implements Comparator<JSONObject> {
        public int compare(JSONObject a, JSONObject b) {
            String val1 = "", val2 = "";
            try {
                val1 = a.getJSONObject("pickup").getString("date");
                val2 = b.getJSONObject("pickup").getString("date");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return val1.compareTo(val2);
        }
    }

    class MyComparatorByDeliveryDate implements Comparator<JSONObject> {
        public int compare(JSONObject a, JSONObject b) {
            String val1 = "", val2 = "";
            try {
                val1 = a.getJSONObject("dropoff").getString("date");
                val2 = b.getJSONObject("dropoff").getString("date");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return val1.compareTo(val2);
        }
    }
}
