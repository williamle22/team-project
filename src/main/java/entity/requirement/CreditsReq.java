package entity.requirement;

import java.util.Objects;
import java.util.Optional;

/**
 * A {@code credits} node: met when the student has at least {@code amount} credits from courses that match
 * {@code filter}.
 */
public final class CreditsReq extends Requirement {
    private final double amount;
    private final Double maxAmount;
    private final Double weight;
    private final CourseFilter filter;

    public CreditsReq(double amount, Double maxAmount, Double weight, CourseFilter filter, boolean concurrent) {
        super(concurrent);
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0, was " + amount);
        }
        if (maxAmount != null && maxAmount < 0) {
            throw new IllegalArgumentException("maxAmount must be >= 0, was " + maxAmount);
        }
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("weight must be >= 0, was " + weight);
        }
        this.amount = amount;
        this.maxAmount = maxAmount;
        this.weight = weight;
        this.filter = Objects.requireNonNull(filter, "filter");
    }

    public double getAmount() {
        return amount;
    }

    /**
     * Returns the cap on matching credits that count, or empty for no cap. A cap of 0, used by limit rows, means no
     * matching credits count.
     */
    public Optional<Double> getMaxAmount() {
        return Optional.ofNullable(maxAmount);
    }

    /** Returns the credits each matching course counts as, or empty to use each course's own weight. */
    public Optional<Double> getWeight() {
        return Optional.ofNullable(weight);
    }

    public CourseFilter getFilter() {
        return filter;
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitCredits(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        CreditsReq that = (CreditsReq) o;
        return amount == that.amount && Objects.equals(maxAmount, that.maxAmount)
                && Objects.equals(weight, that.weight) && filter.equals(that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), amount, maxAmount, weight, filter);
    }
}
