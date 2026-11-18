package use_case.search_courses;

import java.util.List;
import java.util.Objects;

public final class SearchCoursesOutputData {
    private final String query;
    private final List<SearchResultItem> results;
    private final int totalMatches;

    public SearchCoursesOutputData(String query, List<SearchResultItem> results, int totalMatches) {
        this.query = Objects.requireNonNull(query, "query");
        this.results = List.copyOf(results);
        this.totalMatches = totalMatches;
    }

    public String getQuery() {
        return query;
    }

    public List<SearchResultItem> getResults() {
        return results;
    }

    public int getTotalMatches() {
        return totalMatches;
    }

    public boolean isTruncated() {
        return totalMatches > results.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchCoursesOutputData)) {
            return false;
        }
        SearchCoursesOutputData that = (SearchCoursesOutputData) o;
        return query.equals(that.query) && results.equals(that.results) && totalMatches == that.totalMatches;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, results, totalMatches);
    }

    @Override
    public String toString() {
        return "SearchCoursesOutputData{\"" + query + "\", " + results.size() + " of " + totalMatches + "}";
    }
}
