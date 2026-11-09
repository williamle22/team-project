package entity.catalog;

public enum Campus {
    ONLINE("online"), UTSG("utsg"), UTSC("utsc"), UTM("utm");

    private final String value;

    Campus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Campus fromString(String value) throws IllegalArgumentException {
        for (Campus c : values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("unknown campus: " + value);
    }
}
