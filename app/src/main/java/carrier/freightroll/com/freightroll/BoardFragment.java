package carrier.freightroll.com.freightroll;


import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 */
public class BoardFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private enum TRUCKTYPES { SHOW_ALL, FLATBED, DRY_VAN, STEP_DECK, LTL }
    private MapView _mapView;
    private GoogleMap _gmap;
    private Marker mMarcadorActual;
    private LinearLayout _btnDistance;
    private RelativeLayout _btnTruckType;
    private LinearLayout _btnPosition;
    private LinearLayout _btnDate;
    private Dialog _distanceDialog;
    private Dialog _truckDialog;
    private int _iDistance;
    private TRUCKTYPES _eTruckType;
    private int _iYear;
    private int _iMonth;
    private int _iDay;
    private TextView _txtDate;

    public BoardFragment() {
        // Required empty public constructor
        _iDistance = 200;
        _eTruckType = TRUCKTYPES.SHOW_ALL;

        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        _iYear = cal.get(Calendar.YEAR);
        _iMonth = cal.get(Calendar.MONTH);
        _iDay = cal.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_board, container, false);

        _mapView = rootView.findViewById(R.id.mapView);
        _mapView.onCreate(savedInstanceState);

        _mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        _mapView.getMapAsync(this);


        _btnDistance = rootView.findViewById(R.id.ly_distanceLevel);
        _btnDistance.setOnClickListener(this);

        _btnTruckType = rootView.findViewById(R.id.rl_trucktype);
        _btnTruckType.setOnClickListener(this);

        _btnPosition = rootView.findViewById(R.id.ly_position);
        _btnPosition.setOnClickListener(this);

        _btnDate = rootView.findViewById(R.id.ly_date);
        _btnDate.setOnClickListener(this);

        _txtDate = rootView.findViewById(R.id.txt_date);
        showDate();
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _gmap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        _gmap.setMyLocationEnabled(true);
        _gmap.setMinZoomPreference(3);
        LatLng ny = new LatLng(40.7143528, -74.0059731);
        _gmap.moveCamera(CameraUpdateFactory.newLatLng(ny));
        LatLng sydney = new LatLng(-34, 151);
        mMarcadorActual = _gmap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
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

    @Override
    public void onClick(View v) {
        if (v == _btnDistance) {
            showDistanceDialog();
        } else if (v == _btnTruckType) {
            showTruckDialog();
        } else if (v == _btnPosition) {

        } else if (v == _btnDate) {
            showDateDialog();
        }
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            _iYear = selectedYear;
            _iMonth = selectedMonth;
            _iDay = selectedDay;
            showDate();
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
        _distanceDialog.setCancelable(false);
        _distanceDialog.show();
        _distanceDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                setDistanceText();
            }
        });

        Button btn_cancel = _distanceDialog.findViewById(R.id.btn_cancel);
        Button btn_50 = _distanceDialog.findViewById(R.id.btn_show_all);
        Button btn_100 = _distanceDialog.findViewById(R.id.btn_flatbed);
        Button btn_150 = _distanceDialog.findViewById(R.id.btn_dry_van);
        Button btn_200 = _distanceDialog.findViewById(R.id.btn_step_deck);

        switch (_iDistance) {
            case 50:
                btn_50.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 100:
                btn_100.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 150:
                btn_150.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case 200:
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
                _iDistance = 50;
                _distanceDialog.dismiss();
            }
        });

        btn_100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _iDistance = 100;
                _distanceDialog.dismiss();
            }
        });

        btn_150.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _iDistance = 150;
                _distanceDialog.dismiss();
            }
        });

        btn_200.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _iDistance = 200;
                _distanceDialog.dismiss();
            }
        });
    }

    public void showTruckDialog() {
        _truckDialog = new Dialog(getActivity());
        _truckDialog.setContentView(R.layout.truck_layout);
        _truckDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        _truckDialog.setTitle("Custom Alert Dialog");
        _truckDialog.setCancelable(false);
        _truckDialog.show();
        _truckDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                setTruckTypeText();
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
                _eTruckType = TRUCKTYPES.SHOW_ALL;
                _truckDialog.dismiss();
            }
        });

        btn_flatbed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _eTruckType = TRUCKTYPES.FLATBED;
                _truckDialog.dismiss();
            }
        });

        btn_dry_van.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _eTruckType = TRUCKTYPES.DRY_VAN;
                _truckDialog.dismiss();
            }
        });

        btn_step_deck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _eTruckType = TRUCKTYPES.STEP_DECK;
                _truckDialog.dismiss();
            }
        });

        btn_ltl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _eTruckType = TRUCKTYPES.LTL;
                _truckDialog.dismiss();
            }
        });
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

    public void setDistanceText() {
        TextView txtView = getActivity().findViewById(R.id.txtDistance);
        switch (_iDistance) {
            case 50:
                txtView.setText(getResources().getString(R.string.bs_50_mi));
                break;
            case 100:
                txtView.setText(getResources().getString(R.string.bs_100_mi));
                break;
            case 150:
                txtView.setText(getResources().getString(R.string.bs_150_mi));
                break;
            case 200:
                txtView.setText(getResources().getString(R.string.bs_200_mi));
                break;
            default:
                break;
        }
    }

    public void setTruckTypeText() {
        TextView txtView = getActivity().findViewById(R.id.txtTruckType);
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
}
