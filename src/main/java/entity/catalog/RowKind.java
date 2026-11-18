package entity.catalog;

/**
 * The kind of a {@link CompletionRow}. Each value maps to a backend string through {@link #getValue()} and
 * {@link #fromString(String)}. The kinds mean the following:
 * <ul>
 *   <li>{@code ALLOCATION}: claims courses toward the program.
 *   <li>{@code CONSTRAINT}: checks the parent row's courses, or the whole transcript when
 *       {@link CompletionRow#isTranscriptScope()} is true.
 *   <li>{@code LIMIT}: caps how many matching credits count toward the parent row.
 *   <li>{@code CHOICE}: an {@code or} of options. It references each option row by number.
 * </ul>
 */
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
