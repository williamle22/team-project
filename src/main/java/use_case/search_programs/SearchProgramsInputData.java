package use_case.search_programs;

import entity.catalog.ProgramType;

import java.util.Objects;
import java.util.Set;

/**
 * What the user typed and chose. It keeps the query exactly as the user typed it. An empty {@code types} means any
 * program type.
 */
public final class SearchProgramsInputData {
    private final String query;
    private final Set<ProgramType> types;

    public SearchProgramsInputData(String query, Set<ProgramType> types) {
        this.query = Objects.requireNonNull(query, "query");
        this.types = Set.copyOf(Objects.requireNonNull(types, "types"));
    }

    public String getQuery() {
        return query;
    }

    public Set<ProgramType> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchProgramsInputData)) {
            return false;
        }
        SearchProgramsInputData that = (SearchProgramsInputData) o;
        return query.equals(that.query) && types.equals(that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, types);
    }

    @Override
    public String toString() {
        return "SearchProgramsInputData{\"" + query + "\", " + types + "}";
    }
}
