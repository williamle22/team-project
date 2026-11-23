package use_case.search_programs;

import java.util.List;
import java.util.Objects;

public final class SearchProgramsOutputData {
    private final List<ProgramResultItem> results;

    public SearchProgramsOutputData(List<ProgramResultItem> results) {
        this.results = List.copyOf(results);
    }

    public List<ProgramResultItem> getResults() {
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchProgramsOutputData)) {
            return false;
        }
        SearchProgramsOutputData that = (SearchProgramsOutputData) o;
        return results.equals(that.results);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results);
    }

    @Override
    public String toString() {
        return "SearchProgramsOutputData{" + results.size() + " programs}";
    }
}
