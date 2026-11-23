package interface_adapter.search_programs;

import java.util.Objects;

/**
 * One row of the program search results, formatted for display. {@code type} is the type's display name, such as
 * {@code "Major"}.
 */
public final class ProgramResultRow {
    private final String code;
    private final String title;
    private final String type;

    public ProgramResultRow(String code, String title, String type) {
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.type = Objects.requireNonNull(type, "type");
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramResultRow)) {
            return false;
        }
        ProgramResultRow that = (ProgramResultRow) o;
        return code.equals(that.code) && title.equals(that.title) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, type);
    }

    @Override
    public String toString() {
        return "ProgramResultRow{" + code + ", " + title + ", " + type + "}";
    }
}
