package entity.evaluation;

import entity.catalog.ProgramType;

import java.util.Objects;

public final class ProgramProgress {
    private final String programCode;
    private final ProgramType type;
    private final EnrolmentResult enrolment;
    private final CompletionResult completion;

    public ProgramProgress(String programCode, ProgramType type,
                           EnrolmentResult enrolment, CompletionResult completion) {
        this.programCode = Objects.requireNonNull(programCode, "programCode");
        this.type = Objects.requireNonNull(type, "type");
        this.enrolment = Objects.requireNonNull(enrolment, "enrolment");
        this.completion = Objects.requireNonNull(completion, "completion");
    }

    public String getProgramCode() {
        return programCode;
    }

    public ProgramType getType() {
        return type;
    }

    public EnrolmentResult getEnrolment() {
        return enrolment;
    }

    public CompletionResult getCompletion() {
        return completion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramProgress)) {
            return false;
        }
        ProgramProgress that = (ProgramProgress) o;
        return programCode.equals(that.programCode)
                && type == that.type
                && enrolment.equals(that.enrolment)
                && completion.equals(that.completion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programCode, type, enrolment, completion);
    }

    @Override
    public String toString() {
        return "ProgramProgress{" + programCode + " " + type + ", enrolment=" + enrolment.getStatus() + ", completion="
                + completion.getOverall() + "}";
    }
}
