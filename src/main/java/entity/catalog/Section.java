package entity.catalog;

public enum Section {
    F("F", "first term"), S("S", "second term"), Y("Y", "both terms");

    private final String value;
    private final String description;

    Section(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static Section fromString(String value) throws IllegalArgumentException {
        for (Section s : values()) {
            if (s.value.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("unknown section: " + value);
    }
}
