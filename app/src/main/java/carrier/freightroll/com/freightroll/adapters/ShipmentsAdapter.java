package carrier.freightroll.com.freightroll.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import carrier.freightroll.com.freightroll.R;

public class ShipmentsAdapter extends RecyclerView.Adapter<ShipmentsAdapter.MyViewHolder> {
    private JSONArray shipmentsList;
    private TextView fromPlace, toPlace, fromDate, toDate, distance, price;

    class MyViewHolder extends RecyclerView.ViewHolder {
        MyViewHolder(View v) {
            super(v);
            fromPlace = v.findViewById(R.id.txt_from_place);
            fromDate = v.findViewById(R.id.txt_from_date);
            toPlace = v.findViewById(R.id.txt_to_place);
            toDate = v.findViewById(R.id.txt_to_date);
            distance = v.findViewById(R.id.txt_distance);
            price = v.findViewById(R.id.txt_price);
        }
    }

    public ShipmentsAdapter(JSONArray shipmentsList) {
        this.shipmentsList = shipmentsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.shipment_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        try {
            JSONObject shipment = shipmentsList.getJSONObject(position);
            JSONObject fromObj = shipment.getJSONObject("pickup");
            JSONObject toObj = shipment.getJSONObject("dropoff");
            if (fromObj != null) {
                String place = combineCityAndState(fromObj.getString("city"), fromObj.getString("state"));
                fromPlace.setText(place);
                String date = combineStartAndEndDate(fromObj.getString("date"), fromObj.getString("latest_date"));
                fromDate.setText(date);
            }
            if (toObj != null) {
                String place = combineCityAndState(toObj.getString("city"), toObj.getString("state"));
                toPlace.setText(place);
                String date = combineStartAndEndDate(toObj.getString("date"), toObj.getString("latest_date"));
                toDate.setText(date);
            }
            price.setText(convertCurrency(shipment.getDouble("rate")));
            distance.setText(shipment.getString("distance") + " mi.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return shipmentsList.length();
    }

    public String combineCityAndState(String city, String state) {
        String place = city;
        place = (place != null) ? place + ", " : "";
        place += state != null ? state : "";
        if (place.substring(place.length() - 1) == " ") {
            place = place.substring(0, place.length() - 2);
        }

        return place;
    }

    public String convertCurrency(double price) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        return format.format(price);
    }

    public String getDate(String dateString) throws ParseException {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        DateFormat fdf = new SimpleDateFormat("MMM dd");
        return fdf.format(sdf.parse(dateString));
    }

    public String combineStartAndEndDate(String start_date, String end_date) {
        try {
            start_date = getDate(start_date);
            end_date = getDate(end_date);
            if (start_date == end_date) {
                return  start_date;
            } else {
                return  start_date + " - " + end_date;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

}
