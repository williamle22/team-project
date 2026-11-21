package interface_adapter.check_eligibility;

import java.util.List;
import java.util.Objects;

/**
 * One line of the eligibility tree, as plain strings. {@code status} is {@code "met"}, {@code "not met"}, or {@code
 * "review"}, and {@code reason} is empty if there's nothing to explain.
 */
public final class EligibilityLineView {
    private final String text;
    private final String status;
    private final String reason;
    private final List<EligibilityLineView> children;

    public EligibilityLineView(String text, String status, String reason, List<EligibilityLineView> children) {
        this.text = Objects.requireNonNull(text, "text");
        this.status = Objects.requireNonNull(status, "status");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.children = List.copyOf(Objects.requireNonNull(children, "children"));
    }

    public String getText() {
        return text;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public List<EligibilityLineView> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EligibilityLineView)) {
            return false;
        }
        EligibilityLineView that = (EligibilityLineView) o;
        return text.equals(that.text)
                && status.equals(that.status)
                && reason.equals(that.reason)
                && children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, status, reason, children);
    }

    @Override
    public String toString() {
        return "EligibilityLineView{" + status + " \"" + text + "\", reason=" + reason + ", children=" + children
                + "}";
    }
}
