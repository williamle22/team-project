package entity.catalog;

public enum Section {
    F("first term"), S("second term"), Y("both terms");

    private final String section;

    Section(String section) {
        this.section = section;
    }

    public String getSection() {
        return this.section;
    }

    public static Section fromString(String value) throws IllegalArgumentException {
        for (Section s : values()) {
            if (s.section.equals(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("unknown section: " + value);
    }
}
