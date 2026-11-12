package entity.timetable;

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
