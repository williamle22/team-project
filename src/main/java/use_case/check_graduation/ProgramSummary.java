package use_case.check_graduation;

import java.util.List;
import java.util.Objects;

public final class ProgramSummary {
    private final String code;
    private final String title;
    private final RowLine enrolment;
    private final RowLine completion;
    private final List<String> notes;

    public ProgramSummary(String code, String title, RowLine enrolment, RowLine completion, List<String> notes) {
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.enrolment = Objects.requireNonNull(enrolment, "enrolment");
        this.completion = Objects.requireNonNull(completion, "completion");
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public RowLine getEnrolment() {
        return enrolment;
    }

    public RowLine getCompletion() {
        return completion;
    }

    public List<String> getNotes() {
        return notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramSummary)) {
            return false;
        }
        ProgramSummary that = (ProgramSummary) o;
        return code.equals(that.code)
                && title.equals(that.title)
                && enrolment.equals(that.enrolment)
                && completion.equals(that.completion)
                && notes.equals(that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, enrolment, completion, notes);
    }

    @Override
    public String toString() {
        return "ProgramSummary{" + code + " \"" + title + "\", enrolment=" + enrolment.getStatus() + ", completion="
                + completion.getStatus() + ", notes=" + notes + "}";
    }
}
