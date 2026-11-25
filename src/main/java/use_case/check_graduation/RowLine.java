package use_case.check_graduation;

import entity.evaluation.Status;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class RowLine {
    private final String label;
    private final String text;
    private final Status status;
    private final String reason;
    private final List<String> claimed;
    private final List<RowLine> children;

    public RowLine(String label, String text, Status status, String reason, List<String> claimed,
                   List<RowLine> children) {
        this.label = Objects.requireNonNull(label, "label");
        this.text = Objects.requireNonNull(text, "text");
        this.status = Objects.requireNonNull(status, "status");
        this.reason = reason;
        this.claimed = List.copyOf(Objects.requireNonNull(claimed, "claimed"));
        this.children = List.copyOf(Objects.requireNonNull(children, "children"));
    }

    public String getLabel() {
        return label;
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

    public List<String> getClaimed() {
        return claimed;
    }

    public List<RowLine> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RowLine)) {
            return false;
        }
        RowLine that = (RowLine) o;
        return label.equals(that.label)
                && text.equals(that.text)
                && status == that.status
                && Objects.equals(reason, that.reason)
                && claimed.equals(that.claimed)
                && children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, text, status, reason, claimed, children);
    }

    @Override
    public String toString() {
        return "RowLine{" + label + ": " + status + " \"" + text + "\", reason=" + reason + ", claimed=" + claimed
                + ", children=" + children + "}";
    }
}
