package entity.requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** An {@code and} node: met when every item is met. */
public final class AndReq extends Requirement {
    private final List<Requirement> items;
    private final Double weight;

    public AndReq(List<Requirement> items, Double weight, boolean concurrent) {
        super(concurrent);
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        if (weight != null && (!Double.isFinite(weight) || weight < 0)) {
            throw new IllegalArgumentException("weight must be >= 0, was " + weight);
        }
        this.weight = weight;
    }

    public List<Requirement> getItems() {
        return items;
    }

    /**
     * Returns the credits this node contributes when it's a pair of courses in a course list that count only together.
     * It's empty for any other {@code and}.
     */
    public Optional<Double> getWeight() {
        return Optional.ofNullable(weight);
    }

    /** Returns whether this node is a course pair: {@code weight} is set and every item is a {@link CourseReq}. */
    public boolean isCoursePair() {
        return getWeight().isPresent() && items.stream().allMatch(i -> i instanceof CourseReq);
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitAnd(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        AndReq that = (AndReq) o;
        return items.equals(that.items) && Objects.equals(weight, that.weight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), items, weight);
    }

    @Override
    public String toString() {
        return "AndReq{" + items + (weight == null ? "" : ", weight " + weight) + (isConcurrent() ? ", concurrent" : "") + "}";
    }
}
