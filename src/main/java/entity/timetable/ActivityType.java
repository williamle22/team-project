package entity.timetable;

/**
 * A kind of course section: lecture, tutorial, or practical. Each value matches an EASI {@code teachMethod} string
 * through {@link #getValue()} and {@link #fromString(String)}.
 */
public enum ActivityType {
    LEC("LEC"), TUT("TUT"), PRA("PRA");

    private final String value;

    ActivityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ActivityType fromString(String value) {
        for (ActivityType t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("unknown activity type: " + value);
    }
}
