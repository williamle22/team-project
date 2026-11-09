package entity.catalog;

public enum EnrolmentKind {
    OPEN("open"), LIMITED("limited"), FOCUS("focus"), SUSPENDED("suspended");

    private final String value;

    EnrolmentKind(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static EnrolmentKind fromString(String value) throws IllegalArgumentException {
        for (EnrolmentKind e : values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("unknown enrolment kind: " + value);
    }
}
