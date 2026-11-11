package entity.requirement;

/**
 * The reason a {@code text} node has no automatic check. Each value maps to a backend string through
 * {@link #getValue()} and {@link #fromString(String)}.
 */
public enum TextKind {
    PERMISSION("permission"), MEMBERSHIP("membership"), PLACEMENT("placement"), PREPARATION("preparation"),
    ADMISSION("admission"), AMBIGUITY("ambiguity"), CONTENT("content"), OTHER("other");

    private final String value;

    TextKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static TextKind fromString(String value) throws IllegalArgumentException {
        for (TextKind t : values()) {
            if (t.value.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("unknown text kind: " + value);
    }
}
