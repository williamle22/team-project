package entity.catalog;

public enum Campus {
    ONLINE("online"), UTSG("utsg"), UTSC("utsc"), UTM("utm");

    private final String campus;

    Campus(String campus) {
        this.campus = campus;
    }

    public String getCampus() {
        return this.campus;
    }

    public static Campus fromString(String value) throws IllegalArgumentException {
        for (Campus c : values()) {
            if (c.campus.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("unknown campus: " + value);
    }
}
