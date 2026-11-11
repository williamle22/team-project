package entity.catalog;

/**
 * One of the two periods of an academic year. Each value maps to a backend string through {@link #getValue()} and
 * {@link #fromString(String)}.
 */
public enum SessionPeriod {
    FALL_WINTER("fall_winter"), SUMMER("summer");

    private final String value;

    SessionPeriod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SessionPeriod fromString(String value) {
        for (SessionPeriod p : values()) {
            if (p.value.equals(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException("unknown session period: " + value);
    }
}

