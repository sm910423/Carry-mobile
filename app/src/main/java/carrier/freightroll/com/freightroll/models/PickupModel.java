package carrier.freightroll.com.freightroll.models;

import java.util.Date;

public class PickupModel {
    private String address;
    private String address2;
    private String city;
    private String state;
    private String zip_code;
    private Date date;
    private Date latest_date;
    private double lat;
    private double lng;
    private String other_instructions;
    private boolean appointment_needed;
    private String company_name;
    private String contact_person;
    private String phone;
    private GeofencingModel geofencingModel;

    PickupModel (
            String address, String address2, String city, String state, String zip_code,
            Date date, Date latest_date, double lat, double lng, String other_instructions,
            boolean appointment_needed, String company_name, String contact_person, String phone, GeofencingModel geofencingModel
    ) {
        this.address = address;
        this.address2 = address2;
        this.city = city;
        this.state = state;
        this.zip_code = zip_code;
        this.date = date;
        this.latest_date = latest_date;
        this.lat = lat;
        this.lng = lng;
        this.other_instructions = other_instructions;
        this.appointment_needed = appointment_needed;
        this.company_name = company_name;
        this.contact_person = contact_person;
        this.phone = phone;
        this.geofencingModel = geofencingModel;
    }
}
