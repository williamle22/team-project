package entity.catalog;

public enum EnrolmentKind {
    OPEN("open"), LIMITED("limited"), FOCUS("focus"), SUSPENDED("suspended");

    private final String enrolmentKind;

    EnrolmentKind(String enrolmentKind) {
        this.enrolmentKind = enrolmentKind;
    }

    public String getEnrolmentKind() {
        return this.enrolmentKind;
    }

    public static EnrolmentKind fromString(String value) throws IllegalArgumentException {
        for (EnrolmentKind e : values()) {
            if (e.enrolmentKind.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("unknown enrolment kind: " + value);
    }
}
