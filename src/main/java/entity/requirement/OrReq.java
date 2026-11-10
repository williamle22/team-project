package entity.requirement;

import java.util.List;
import java.util.Objects;

public final class OrReq extends Requirement {
    private final List<Requirement> items;

    public OrReq(List<Requirement> items, boolean concurrent) {
        super(concurrent);
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
    }

    public List<Requirement> getItems() {
        return items;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitOr(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        OrReq that = (OrReq) o;
        return items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), items);
    }

    @Override
    public String toString() {
        return "OrReq{" + items + (isConcurrent() ? ", concurrent" : "") + "}";
    }
}
