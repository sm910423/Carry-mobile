package carrier.freightroll.com.freightroll.models;

public class AdditionalServiceModel {
    private String name;
    private String description;
    private int quantity;
    private int price_per_unit;
    private String unit_label;

    AdditionalServiceModel(String name, String description, int quantity, int price_per_unit, String unit_label) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price_per_unit = price_per_unit;
        this.unit_label = unit_label;
    }
}
