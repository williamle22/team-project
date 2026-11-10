package entity.requirement;

import java.util.List;
import java.util.Objects;

public final class RowRefReq extends Requirement {
    private final List<Integer> rows;

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
