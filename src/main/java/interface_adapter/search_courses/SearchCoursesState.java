package interface_adapter.search_courses;

import java.util.List;
import java.util.Objects;

/**
 * What the search panel shows. If {@code message} isn't empty, the panel shows it instead of the results. The panel
 * shows {@code footer} under the results when more courses matched than the list holds. Otherwise {@code footer} is
 * empty.
 */
public final class SearchCoursesState {
    private List<SearchResultRow> results = List.of();
    private String message = "";
    private String footer = "";

    public List<SearchResultRow> getResults() {
        return results;
    }

    public void setResults(List<SearchResultRow> results) {
        this.results = List.copyOf(results);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchCoursesState)) {
            return false;
        }
        SearchCoursesState that = (SearchCoursesState) o;
        return Objects.equals(results, that.results) && Objects.equals(message, that.message)
                && Objects.equals(footer, that.footer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, message, footer);
    }

    @Override
    public String toString() {
        return "SearchCoursesState{" + results.size() + " results, " + message + ", " + footer + "}";
    }
}
