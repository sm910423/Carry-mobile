package carrier.freightroll.com.freightroll.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import carrier.freightroll.com.freightroll.R;
import carrier.freightroll.com.freightroll.helpers.FunctionManager;

public class ShipmentsAdapter extends RecyclerView.Adapter<ShipmentsAdapter.MyViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(JSONObject item);
    }

    private JSONArray shipmentsList;
    private TextView fromPlace, toPlace, fromDate, toDate, distance, price;
    private final OnItemClickListener listener;

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

        public void bind(final JSONObject item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public ShipmentsAdapter(JSONArray shipmentsList, OnItemClickListener listener) {
        this.shipmentsList = shipmentsList;
        this.listener = listener;
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
                String place = FunctionManager.combineCityAndState(fromObj.getString("city"), fromObj.getString("state"));
                fromPlace.setText(place);
                String date = FunctionManager.combineStartAndEndDate(fromObj.getString("date"), fromObj.getString("latest_date"));
                fromDate.setText(date);
            }
            if (toObj != null) {
                String place = FunctionManager.combineCityAndState(toObj.getString("city"), toObj.getString("state"));
                toPlace.setText(place);
                String date = FunctionManager.combineStartAndEndDate(toObj.getString("date"), toObj.getString("latest_date"));
                toDate.setText(date);
            }
            price.setText(FunctionManager.convertCurrency(shipment.getDouble("rate")));
            distance.setText(shipment.getString("distance") + " mi.");

            holder.bind(shipment, listener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return shipmentsList.length();
    }

}
