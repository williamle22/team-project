package entity.catalog;

/**
 * A program's type. Each value maps to a backend string through {@link #getValue()} and {@link #fromString(String)}.
 */
public enum ProgramType {
    SPECIALIST("Specialist"), MAJOR("Major"), MINOR("Minor"), FOCUS("Focus"), CERTIFICATE("Certificate");

    private final String value;

    ProgramType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static ProgramType fromString(String value) throws IllegalArgumentException {
        for (ProgramType p : values()) {
            if (p.value.equals(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException("unknown program type: " + value);
    }
}
