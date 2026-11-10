package entity.requirement;

import java.util.Objects;

public final class TextReq extends Requirement {
    private final TextKind kind;
    private final String text;

    public TextReq(TextKind kind, String text, boolean concurrent) {
        super(concurrent);
        this.kind = Objects.requireNonNull(kind, "kind");
        this.text = Objects.requireNonNull(text, "text");
    }

    public TextKind getKind() {
        return kind;
    }

    public String getText() {
        return text;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitText(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        TextReq that = (TextReq) o;
        return kind == that.kind && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), kind, text);
    }
}
