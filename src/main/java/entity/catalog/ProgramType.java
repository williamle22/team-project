package entity.catalog;

public enum ProgramType {
    SPECIALIST("Specialist"), MAJOR("Major"), MINOR("Minor"), FOCUS("FOCUS"), CERTIFICATE("Certificate");

    private final String programType;

    ProgramType(String programType) {
        this.programType = programType;
    }

    public String getProgramType() {
        return this.programType;
    }

    public static ProgramType fromString(String value) throws IllegalArgumentException {
        for (ProgramType p : values()) {
            if (p.programType.equals(value)) {
                return p;
            }
        }
        throw new IllegalArgumentException("unknown program type: " + value);
    }
}
