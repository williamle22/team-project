package entity.evaluation;

import entity.catalog.CompletionRow;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class RowResult {
    private final CompletionRow row;
    private final boolean applicable;
    private final Status status;
    private final String reason;
    private final EvaluationNode detail;
    private final List<String> claimed;

    public RowResult(CompletionRow row, boolean applicable, Status status, String reason,
                     EvaluationNode detail, List<String> claimed) {
        this.row = Objects.requireNonNull(row, "row");
        this.applicable = applicable;
        this.status = Objects.requireNonNull(status, "status");
        this.reason = reason;
        this.detail = detail;
        this.claimed = List.copyOf(Objects.requireNonNull(claimed, "claimed"));
    }

    public CompletionRow getRow() {
        return row;
    }

    public int getNumber() {
        return row.getNumber();
    }

    public boolean isApplicable() {
        return applicable;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    public Optional<EvaluationNode> getDetail() {
        return Optional.ofNullable(detail);
    }

    public List<String> getClaimed() {
        return claimed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RowResult)) {
            return false;
        }
        RowResult that = (RowResult) o;
        return applicable == that.applicable
                && row.equals(that.row)
                && status == that.status
                && Objects.equals(reason, that.reason)
                && Objects.equals(detail, that.detail)
                && claimed.equals(that.claimed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, applicable, status, reason, detail, claimed);
    }

    @Override
    public String toString() {
        return "RowResult{" + getNumber() + ", " + status + ", applicable=" + applicable + ", reason=" + reason
                + ", claimed=" + claimed + "}";
    }
}
