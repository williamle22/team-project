package entity.requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An {@code average} node: met when the average grade of the matching courses is at least {@code minGrade} percent.
 * {@code minGrade} is the required average, not a per-course minimum.
 */
public final class AverageReq extends Requirement {
    private final int minGrade;
    private final Double amount;
    private final boolean best;
    private final CourseFilter filter;
    private final List<Requirement> items;

    /** @throws IllegalArgumentException if {@code filter} sets {@code minGrade} */
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

    /** Returns the credits the average covers, or empty to average all matching courses. */
    public Optional<Double> getAmount() {
        return Optional.ofNullable(amount);
    }

    /** Returns whether only the best {@code amount} credits count toward the average. */
    public boolean isBest() {
        return best;
    }

    public CourseFilter getFilter() {
        return filter;
    }

    /**
     * Returns the sub-requirements, such as {@code credits}, {@code or}, and {@code choose} nodes, whose courses form
     * the averaged set.
     */
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
