package entity.requirement;

import java.util.Objects;
import java.util.Optional;

/** A {@code gpa} node: met when the student's GPA is at least {@code minGpa}, on a 0-4 scale. */
public final class GpaReq extends Requirement {
    private final double minGpa;
    private final String scope;

    public GpaReq(double minGpa, String scope, boolean concurrent) {
        super(concurrent);
        if (Double.isNaN(minGpa) || minGpa < 0 || minGpa > 4) {
            throw new IllegalArgumentException("minGpa must be 0-4, was " + minGpa);
        }
        this.minGpa = minGpa;
        this.scope = scope;
    }

    public double getMinGpa() {
        return minGpa;
    }

    /**
     * Returns free text naming the courses the GPA covers, such as {@code CHM credits}, or empty for the overall GPA.
     */
    public Optional<String> getScope() {
        return Optional.ofNullable(scope);
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitGpa(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        GpaReq that = (GpaReq) o;
        return Double.compare(minGpa, that.minGpa) == 0 && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minGpa, scope);
    }
}
