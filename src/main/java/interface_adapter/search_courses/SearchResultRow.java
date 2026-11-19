package interface_adapter.search_courses;

import java.util.Objects;

public final class SearchResultRow {
    private final String code;
    private final String title;
    private final String credits;
    private final String details;

    public SearchResultRow(String code, String title, String credits, String details) {
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.credits = Objects.requireNonNull(credits, "credits");
        this.details = Objects.requireNonNull(details, "details");
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getCredits() {
        return credits;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchResultRow)) {
            return false;
        }
        SearchResultRow that = (SearchResultRow) o;
        return code.equals(that.code) && title.equals(that.title) && credits.equals(that.credits)
                && details.equals(that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, credits, details);
    }

    @Override
    public String toString() {
        return "SearchResultRow{" + code + ", " + title + ", " + credits + ", " + details + "}";
    }
}
