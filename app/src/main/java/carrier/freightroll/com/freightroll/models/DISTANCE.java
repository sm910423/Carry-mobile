package carrier.freightroll.com.freightroll.models;

public enum DISTANCE {
    D_200(0), D_150(1), D_100(2), D_50(3);

    private final int _value;

    DISTANCE(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }
}
