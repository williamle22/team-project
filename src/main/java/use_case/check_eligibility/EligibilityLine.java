package use_case.check_eligibility;

import entity.evaluation.Status;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One line of a course's eligibility tree, ready for display. {@code reason} is {@code null} if there's nothing to
 * explain.
 */
public final class EligibilityLine {
    private final String text;
    private final Status status;
    private final String reason;
    private final List<EligibilityLine> children;

    public EligibilityLine(String text, Status status, String reason, List<EligibilityLine> children) {
        this.text = Objects.requireNonNull(text, "text");
        this.status = Objects.requireNonNull(status, "status");
        this.reason = reason;
        this.children = List.copyOf(Objects.requireNonNull(children, "children"));
    }

    public String getText() {
        return text;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getReason() {
        return Optional.ofNullable(reason);
    }

    public List<EligibilityLine> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EligibilityLine)) {
            return false;
        }
        EligibilityLine that = (EligibilityLine) o;
        return text.equals(that.text)
                && status == that.status
                && Objects.equals(reason, that.reason)
                && children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, status, reason, children);
    }

    @Override
    public String toString() {
        return "EligibilityLine{" + status + " \"" + text + "\", reason=" + reason + ", children=" + children + "}";
    }
}
