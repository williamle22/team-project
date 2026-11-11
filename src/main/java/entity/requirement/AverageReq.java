package entity.requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AverageReq extends Requirement {
    private final int minGrade;
    private final Double amount;
    private final boolean best;
    private final CourseFilter filter;
    private final List<Requirement> items;

    public AverageReq(int minGrade, Double amount, boolean best, CourseFilter filter, List<Requirement> items,
                      boolean concurrent) {
        super(concurrent);
        if (minGrade < 0 || minGrade > 100) {
            throw new IllegalArgumentException("minGrade must be 0-100, was " + minGrade);
        }
        this.minGrade = minGrade;
        this.amount = amount;
        this.best = best;
        this.filter = Objects.requireNonNull(filter, "filter");
        if (filter.getMinGrade().isPresent()) {
            throw new IllegalArgumentException("filter must not set minGrade, was " + filter);
        }
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
    }

    public int getMinGrade() {
        return minGrade;
    }

    public Optional<Double> getAmount() {
        return Optional.ofNullable(amount);
    }

    public boolean isBest() {
        return best;
    }

    public CourseFilter getFilter() {
        return filter;
    }

    public List<Requirement> getItems() {
        return items;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitAverage(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        AverageReq that = (AverageReq) o;
        return minGrade == that.minGrade && Objects.equals(amount, that.amount) && best == that.best
                && filter.equals(that.filter) && items.equals(that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minGrade, amount, best, filter, items);
    }
}
