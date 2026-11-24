package entity.evaluation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CompletionResult {
    private final List<RowResult> rows;
    private final Status overall;
    private final List<String> warnings;

    public CompletionResult(List<RowResult> rows, Status overall, List<String> warnings) {
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
        this.overall = Objects.requireNonNull(overall, "overall");
        this.warnings = List.copyOf(Objects.requireNonNull(warnings, "warnings"));
    }

    public List<RowResult> getRows() {
        return rows;
    }

    public Status getOverall() {
        return overall;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Optional<RowResult> row(int number) {
        return rows.stream().filter(r -> r.getNumber() == number).findFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompletionResult)) {
            return false;
        }
        CompletionResult that = (CompletionResult) o;
        return rows.equals(that.rows) && overall == that.overall && warnings.equals(that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, overall, warnings);
    }

    @Override
    public String toString() {
        return "CompletionResult{" + overall + ", rows=" + rows + ", warnings=" + warnings + "}";
    }
}
