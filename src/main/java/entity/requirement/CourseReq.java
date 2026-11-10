package entity.requirement;

import java.util.Objects;
import java.util.Optional;

public final class CourseReq extends Requirement {
    private final String code;
    private final Integer minGrade;

    public CourseReq(String code, Integer minGrade, boolean concurrent) {
        super(concurrent);
        this.code = Objects.requireNonNull(code, "code");
        if (code.isBlank()) {
            throw new IllegalArgumentException("code is blank");
        }
        if (minGrade != null && (minGrade < 0 || minGrade > 100)) {
            throw new IllegalArgumentException("minGrade must be 0-100, was " + minGrade);
        }
        this.minGrade = minGrade;
    }

    public String getCode() {
        return code;
    }

    public Optional<Integer> getMinGrade() {
        return Optional.ofNullable(minGrade);
    }

    @Override
    public <R> R accept(RequirementVisitor<R> visitor) {
        return visitor.visitCourse(this);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        CourseReq that = (CourseReq) o;
        return code.equals(that.code) && Objects.equals(minGrade, that.minGrade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), code, minGrade);
    }

    @Override
    public String toString() {
        return "CourseReq{" + code + (minGrade == null ? "" : ", min " + minGrade) + (isConcurrent() ? ", concurrent" : "") + "}";
    }
}
