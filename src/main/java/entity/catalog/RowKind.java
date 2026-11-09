package entity.catalog;

public enum RowKind {
    ALLOCATION("allocation"), CONSTRAINT("constraint"), LIMIT("limit"), CHOICE("choice");

    private final String value;

    RowKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static RowKind fromString(String value) throws IllegalArgumentException {
        for (RowKind r : values()) {
            if (r.value.equals(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("unknown row kind: " + value);
    }
}
