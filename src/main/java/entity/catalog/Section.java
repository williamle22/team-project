package entity.catalog;

/**
 * The part of a {@link SessionPeriod} that a course runs in. Each value maps to a backend string through
 * {@link #getValue()} and {@link #fromString(String)}.
 *
 * <p>In {@code FALL_WINTER}, {@code F} is the first term (fall), {@code S} is the second term (winter), and
 * {@code Y} is both terms (full year). In {@code SUMMER}, {@code F} and {@code S} are the two summer sub-sessions.
 */
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
