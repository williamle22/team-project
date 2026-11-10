package entity.requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AndReq extends Requirement {
    private final List<Requirement> items;
    private final Double weight;

    public AndReq(List<Requirement> items, Double weight, boolean concurrent) {
        super(concurrent);
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("weight must be >= 0, was " + weight);
        }
        this.weight = weight;
    }

    public List<Requirement> getItems() {
        return items;
    }

    public Optional<Double> getWeight() {
        return Optional.ofNullable(weight);
    }

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
