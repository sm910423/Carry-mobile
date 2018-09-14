package carrier.freightroll.com.freightroll.models;

public class AlertModel {
    private String alert;
    private String sound;
    private String link;
    private String payload;

    AlertModel(String alert, String sound, String link, String payload) {
        this.alert = alert;
        this.sound = sound;
        this.link = link;
        this.payload = payload;
    }
}
