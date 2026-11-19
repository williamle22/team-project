package use_case.search_courses;

import java.util.List;
import java.util.Objects;

public final class SearchCoursesOutputData {
    private final List<SearchResultItem> results;
    private final int totalMatches;

    public SearchCoursesOutputData(List<SearchResultItem> results, int totalMatches) {
        this.results = List.copyOf(results);
        this.totalMatches = totalMatches;
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
        return results.equals(that.results) && totalMatches == that.totalMatches;
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, totalMatches);
    }

    @Override
    public String toString() {
        return "SearchCoursesOutputData{" + results.size() + " of " + totalMatches + "}";
    }
}
