package carrier.freightroll.com.freightroll.models;

public enum SORTOPTION {
    RATE(0), RELEVANCE(1), DISTANCE(2), PICKUP_DATE(3), DELIVERY_DATE(4);

    private final int _value;

    SORTOPTION(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }
}
