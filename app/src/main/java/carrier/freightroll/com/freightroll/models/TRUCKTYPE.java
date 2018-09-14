package carrier.freightroll.com.freightroll.models;

public enum TRUCKTYPE {
    SHOW_ALL(0), FLATBED(1), DRY_VAN(2), STEP_DECK(3), LTL(4);

    private final int _value;

    TRUCKTYPE(int value) {
        _value = value;
    }

    public int getValue() {
        return _value;
    }
}

