package entity.evaluation;

import java.util.Objects;

public final class ProgramProgress {
    private final String programCode;
    private final EnrolmentResult enrolment;
    private final CompletionResult completion;

    public ProgramProgress(String programCode, EnrolmentResult enrolment, CompletionResult completion) {
        this.programCode = Objects.requireNonNull(programCode, "programCode");
        this.enrolment = Objects.requireNonNull(enrolment, "enrolment");
        this.completion = Objects.requireNonNull(completion, "completion");
    }

    public String getProgramCode() {
        return programCode;
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
                && enrolment.equals(that.enrolment)
                && completion.equals(that.completion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(programCode, enrolment, completion);
    }

    @Override
    public String toString() {
        return "ProgramProgress{" + programCode + ", enrolment=" + enrolment.getStatus() + ", completion="
                + completion.getOverall() + "}";
    }
}
