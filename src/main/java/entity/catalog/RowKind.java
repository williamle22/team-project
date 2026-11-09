package entity.catalog;

public enum RowKind {
    ALLOCATION("allocation"), CONSTRAINT("constraint"), LIMIT("limit"), CHOICE("choice");

    private final String rowKind;

    RowKind(String rowKind) {
        this.rowKind = rowKind;
    }

    public String getRowKind() {
        return this.rowKind;
    }

    public static RowKind fromString(String value) throws IllegalArgumentException {
        for (RowKind r : values()) {
            if (r.rowKind.equals(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("unknown row kind: " + value);
    }
}
