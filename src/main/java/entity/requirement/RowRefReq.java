package entity.requirement;

import java.util.List;
import java.util.Objects;

/**
 * A {@code requirement} node: met when any program completion row numbered in {@code rows} is met. Only program
 * completion trees contain this node.
 */
public final class RowRefReq extends Requirement {
    private final List<Integer> rows;

    /** @throws IllegalArgumentException if {@code rows} is empty */
    public RowRefReq(List<Integer> rows, boolean concurrent) {
        super(concurrent);
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("rows is empty");
        }
    }

    public List<Integer> getRows() {
        return rows;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitRowRef(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        RowRefReq that = (RowRefReq) o;
        return rows.equals(that.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rows);
    }
}
