package entity.requirement;

import java.util.Objects;

/** A {@code not} node: met when its item isn't met. */
public final class NotReq extends Requirement {
    private final Requirement item;

    public NotReq(Requirement item, boolean concurrent) {
        super(concurrent);
        this.item = Objects.requireNonNull(item, "item");
    }

    public Requirement getItem() {
        return item;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitNot(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        NotReq that = (NotReq) o;
        return item.equals(that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), item);
    }
}
