package use_case.search_programs;

import entity.catalog.ProgramType;

import java.util.Objects;

public final class ProgramResultItem {
    private final String code;
    private final String title;
    private final ProgramType type;

    public ProgramResultItem(String code, String title, ProgramType type) {
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

    public ProgramType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramResultItem)) {
            return false;
        }
        ProgramResultItem that = (ProgramResultItem) o;
        return code.equals(that.code) && title.equals(that.title) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, type);
    }

    @Override
    public String toString() {
        return "ProgramResultItem{" + code + ", " + title + ", " + type + "}";
    }
}
