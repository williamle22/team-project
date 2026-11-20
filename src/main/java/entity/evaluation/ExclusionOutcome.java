package entity.evaluation;

import java.util.List;
import java.util.Objects;

public final class ExclusionOutcome {
    private final ExclusionResult result;
    private final List<String> reasons;
    private final List<String> blockingCourses;

    public ExclusionOutcome(ExclusionResult result, List<String> reasons, List<String> blockingCourses) {
        this.result = Objects.requireNonNull(result, "result");
        this.reasons = List.copyOf(Objects.requireNonNull(reasons, "reasons"));
        this.blockingCourses = List.copyOf(Objects.requireNonNull(blockingCourses, "blockingCourses"));
    }

    public ExclusionResult getResult() {
        return result;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public List<String> getBlockingCourses() {
        return blockingCourses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExclusionOutcome)) {
            return false;
        }
        ExclusionOutcome that = (ExclusionOutcome) o;
        return result == that.result
                && reasons.equals(that.reasons)
                && blockingCourses.equals(that.blockingCourses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, reasons, blockingCourses);
    }

    @Override
    public String toString() {
        return "ExclusionOutcome{" + result + ", reasons=" + reasons + ", blocking=" + blockingCourses + "}";
    }
}
