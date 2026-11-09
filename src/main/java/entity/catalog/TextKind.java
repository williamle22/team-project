package entity.catalog;

public enum TextKind {
    PERMISSION("permission"), MEMBERSHIP("membership"), PLACEMENT("placement"), PREPARATION("preparation"),
    ADMISSION("admission"), AMBIGUITY("ambiguity"), CONTENT("content"), OTHER("other");

    private final String textKind;

    TextKind(String textKind) {
        this.textKind = textKind;
    }

    public String getTextKind() {
        return this.textKind;
    }

    public static TextKind fromString(String value) throws IllegalArgumentException {
        for (TextKind t : values()) {
            if (t.textKind.equals(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("unknown text kind: " + value);
    }
}
