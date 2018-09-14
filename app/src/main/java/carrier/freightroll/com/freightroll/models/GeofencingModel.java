package carrier.freightroll.com.freightroll.models;

public class GeofencingModel {
    private int radius;
    private AlertModel on_enter;
    private AlertModel on_exit;

    GeofencingModel(int radius, AlertModel on_enter, AlertModel on_exit) {
        this.radius = radius;
        this.on_enter = on_enter;
        this.on_exit = on_exit;
    }
}
