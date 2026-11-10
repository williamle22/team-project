package entity.requirement;

import java.util.List;
import java.util.Objects;

public final class ChooseReq extends Requirement {
    private final int count;
    private final List<Requirement> items;

    public ChooseReq(int count, List<Requirement> items, boolean concurrent) {
        super(concurrent);
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1, was " + count);
        }
        this.count = count;
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
    }

    public int getCount() {
        return count;
    }

    public List<Requirement> getItems() {
        return items;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitChoose(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        ChooseReq that = (ChooseReq) o;
        return count == that.count && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), count, items);
    }

    @Override
    public String toString() {
        return "ChooseReq{" + count + " of " + items + (isConcurrent() ? ", concurrent" : "") + "}";
    }
}
