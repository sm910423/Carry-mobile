package carrier.freightroll.com.freightroll.helpers;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.models.TRUCKTYPE;

public class FunctionManager {
    public static GoogleMap _gmap;

    public static String combineCityAndState(String city, String state) {
        String place = city;
        place = (place != null) ? place + ", " : "";
        place += state != null ? state : "";
        if (place.substring(place.length() - 1) == " ") {
            place = place.substring(0, place.length() - 2);
        }

        return place;
    }

    public static String convertCurrency(double price) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        return format.format(price);
    }

    public static String getDate(String dateString) throws ParseException {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat fdf = new SimpleDateFormat("MMM dd");
        return fdf.format(sdf.parse(dateString));
    }

    public static String getHour(String dateString) throws ParseException {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat fdf = new SimpleDateFormat("HH:mm z");
        return fdf.format(sdf.parse(dateString));
    }

    public static String combineStartAndEndDate(String start_date, String end_date) {
        try {
            start_date = getDate(start_date);
            end_date = getDate(end_date);
            if (start_date.equalsIgnoreCase(end_date)) {
                return  start_date;
            } else {
                return  start_date + " - " + end_date;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String combineStartAndEndHours(String start_date, String end_date) {
        try {
            start_date = getHour(start_date);
            end_date = getHour(end_date);
            if (start_date.equalsIgnoreCase(end_date)) {
                return  start_date;
            } else {
                return  start_date + " - " + end_date;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getStringTruckType(Activity activity, TRUCKTYPE t) {
        String str = "";
        switch (t) {
            case SHOW_ALL:
                str = activity.getString(R.string.bs_text_showall);
                break;
            case FLATBED:
                str = activity.getString(R.string.bs_text_flatbed);
                break;
            case STEP_DECK:
                str = activity.getString(R.string.bs_text_stepdeck);
                break;
            case DRY_VAN:
                str = activity.getString(R.string.bs_text_dryvan);
                break;
            case LTL:
                str = activity.getString(R.string.bs_text_ltl);
                break;
            default:
                break;
        }
        return str;
    }

    public static TRUCKTYPE getEnumTruckType(String truckTYpe) {
        TRUCKTYPE eType = TRUCKTYPE.SHOW_ALL;

        switch (truckTYpe) {
            case "show_all":
                eType = TRUCKTYPE.SHOW_ALL;
                break;
            case "flatbed":
                eType = TRUCKTYPE.FLATBED;
                break;
            case "dry_van":
                eType = TRUCKTYPE.DRY_VAN;
                break;
            case "step_deck":
                eType = TRUCKTYPE.STEP_DECK;
                break;
            case "ltl":
                eType = TRUCKTYPE.LTL;
                break;
        }

        return eType;
    }

    public static String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=AIzaSyD_Tzoij10Or6sxR9vVAaFpcxFzsQTYawQ";

        return url;
    }

    public static String getStateCode(String address) {
        String stateCode = "";
        String[] pattern = address.split(", ");
        if (pattern.length > 1) {
            String strZipCode = pattern[pattern.length - 2];
            String[] codes = strZipCode.split(" ");
            stateCode = codes[0];
        }
        return stateCode;
    }
}
