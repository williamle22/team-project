package entity.requirement;

import java.util.Objects;
import java.util.Optional;

public final class GpaReq extends Requirement {
    private final double minGpa;
    private final String scope;

    public GpaReq(double minGpa, String scope, boolean concurrent) {
        super(concurrent);
        if (minGpa < 0 || minGpa > 4) {
            throw new IllegalArgumentException("minGpa must be 0-4, was " + minGpa);
        }
        this.minGpa = minGpa;
        this.scope = scope;
    }

    public double getMinGpa() {
        return minGpa;
    }

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
        return minGpa == that.minGpa && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), minGpa, scope);
    }
}
