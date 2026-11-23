package interface_adapter.search_programs;

import java.util.List;
import java.util.Objects;

public final class SearchProgramsState {
    private List<ProgramResultRow> results = List.of();
    private String message = "";

    public List<ProgramResultRow> getResults() {
        return results;
    }

    public void setResults(List<ProgramResultRow> results) {
        this.results = List.copyOf(results);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchProgramsState)) {
            return false;
        }
        SearchProgramsState that = (SearchProgramsState) o;
        return Objects.equals(results, that.results) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, message);
    }

    @Override
    public String toString() {
        return "SearchProgramsState{" + results.size() + " results, " + message + "}";
    }
}
