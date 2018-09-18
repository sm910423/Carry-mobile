package carrier.freightroll.com.freightroll.models;

import java.util.List;

public class TruckModel {
    private int id;
    private TRUCKTYPE truck_type;
    private int distance;
    private int weight;
    private String commodity;
    private float rate;
    private String status;
    private PickupModel pickup;
    private PickupModel dropoff;
    private List<AdditionalServiceModel> additional_services;

    public TruckModel(int id, TRUCKTYPE truck_type, int distance, int weight, String commodity, float rate, String status, PickupModel pickup, PickupModel dropoff, List<AdditionalServiceModel> additional_services) {
        this.id = id;
        this.truck_type = truck_type;
        this.distance = distance;
        this.weight = weight;
        this.commodity = commodity;
        this.rate = rate;
        this.status = status;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.additional_services = additional_services;
    }
}
