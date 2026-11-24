package entity.evaluation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EnrolmentResult {
    private final Status status;
    private final String reason;
    private final EvaluationNode detail;
    private final List<String> notes;

    public EnrolmentResult(Status status, String reason, EvaluationNode detail, List<String> notes) {
        this.status = Objects.requireNonNull(status, "status");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.detail = detail;
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
    }

    public Status getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Optional<EvaluationNode> getDetail() {
        return Optional.ofNullable(detail);
    }

    public List<String> getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EnrolmentResult)) {
            return false;
        }
        EnrolmentResult that = (EnrolmentResult) o;
        return status == that.status
                && reason.equals(that.reason)
                && Objects.equals(detail, that.detail)
                && notes.equals(that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, reason, detail, notes);
    }

    @Override
    public String toString() {
        return "EnrolmentResult{" + status + ", " + reason + ", notes=" + notes + ", detail=" + detail + "}";
    }
}
