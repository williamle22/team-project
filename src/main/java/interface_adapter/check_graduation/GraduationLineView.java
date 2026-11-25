package interface_adapter.check_graduation;

import java.util.List;
import java.util.Objects;

public final class GraduationLineView {
    private final String label;
    private final String text;
    private final String status;
    private final String reason;
    private final String claimed;
    private final List<GraduationLineView> children;

    public GraduationLineView(String label, String text, String status, String reason, String claimed,
                              List<GraduationLineView> children) {
        this.label = Objects.requireNonNull(label, "label");
        this.text = Objects.requireNonNull(text, "text");
        this.status = Objects.requireNonNull(status, "status");
        this.reason = Objects.requireNonNull(reason, "reason");
        this.claimed = Objects.requireNonNull(claimed, "claimed");
        this.children = List.copyOf(Objects.requireNonNull(children, "children"));
    }

    public String getLabel() {
        return label;
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

    public String getClaimed() {
        return claimed;
    }

    public List<GraduationLineView> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraduationLineView)) {
            return false;
        }
        GraduationLineView that = (GraduationLineView) o;
        return label.equals(that.label)
                && text.equals(that.text)
                && status.equals(that.status)
                && reason.equals(that.reason)
                && claimed.equals(that.claimed)
                && children.equals(that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, text, status, reason, claimed, children);
    }

    @Override
    public String toString() {
        return "GraduationLineView{" + label + ": " + status + " \"" + text + "\", reason=" + reason + ", claimed="
                + claimed + ", children=" + children + "}";
    }
}
