package use_case.search_courses;

import java.util.Objects;

public final class SearchCoursesInputData {
    private final String query;
    private final SearchFilters filters;

    public SearchCoursesInputData(String query, SearchFilters filters) {
        this.query = Objects.requireNonNull(query, "query");
        this.filters = Objects.requireNonNull(filters, "filters");
    }

    public String getQuery() {
        return query;
    }

    public SearchFilters getFilters() {
        return filters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchCoursesInputData)) {
            return false;
        }
        SearchCoursesInputData that = (SearchCoursesInputData) o;
        return query.equals(that.query) && filters.equals(that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, filters);
    }

    @Override
    public String toString() {
        return "SearchCoursesInputData{\"" + query + "\", " + filters + "}";
    }
}
