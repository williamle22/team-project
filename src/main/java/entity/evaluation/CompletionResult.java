package entity.evaluation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A program's or degree's completion requirements, evaluated. {@code rows} has a result for every row, in document
 * order, including rows that don't apply. To find one by number, use {@link #row(int)}. {@code overall} combines every
 * row that applies, except the options of a choice row.
 */
public final class CompletionResult {
    private final List<RowResult> rows;
    private final Status overall;

    public CompletionResult(List<RowResult> rows, Status overall) {
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
        this.overall = Objects.requireNonNull(overall, "overall");
    }

    public List<RowResult> getRows() {
        return rows;
    }

    public Status getOverall() {
        return overall;
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
        return rows.equals(that.rows) && overall == that.overall;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows, overall);
    }

    @Override
    public String toString() {
        return "CompletionResult{" + overall + ", rows=" + rows + "}";
    }
}
