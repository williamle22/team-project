package entity.evaluation;

import entity.degree.DegreeType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DegreeProgress {
    private final DegreeType type;
    private final CompletionResult completion;
    private final Status programCombination;
    private final String programCombinationReason;

    public DegreeProgress(DegreeType type, CompletionResult completion, Status programCombination,
                          String programCombinationReason) {
        this.type = Objects.requireNonNull(type, "type");
        this.completion = Objects.requireNonNull(completion, "completion");
        this.programCombination = Objects.requireNonNull(programCombination, "programCombination");
        this.programCombinationReason = programCombinationReason;
    }

    public DegreeType getType() {
        return type;
    }

    public CompletionResult getCompletion() {
        return completion;
    }

    public Status getProgramCombination() {
        return programCombination;
    }

    public Optional<String> getProgramCombinationReason() {
        return Optional.ofNullable(programCombinationReason);
    }

    public Status getOverall() {
        return Status.allOf(List.of(completion.getOverall(), programCombination));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DegreeProgress)) {
            return false;
        }
        DegreeProgress that = (DegreeProgress) o;
        return type == that.type
                && completion.equals(that.completion)
                && programCombination == that.programCombination
                && Objects.equals(programCombinationReason, that.programCombinationReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, completion, programCombination, programCombinationReason);
    }

    @Override
    public String toString() {
        return "DegreeProgress{" + type + ", " + getOverall() + ", completion=" + completion.getOverall()
                + ", programCombination=" + programCombination + "}";
    }
}
