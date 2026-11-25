package interface_adapter.check_graduation;

import java.util.List;
import java.util.Objects;

/**
 * One program's result, formatted for display. {@code status} is the program's completion status, as in
 * {@link GraduationLineView}.
 */
public final class ProgramView {
    private final String code;
    private final String title;
    private final String status;
    private final GraduationLineView enrolment;
    private final GraduationLineView completion;
    private final List<String> notes;

    public ProgramView(String code, String title, String status, GraduationLineView enrolment,
                       GraduationLineView completion, List<String> notes) {
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.status = Objects.requireNonNull(status, "status");
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

    public String getStatus() {
        return status;
    }

    public GraduationLineView getEnrolment() {
        return enrolment;
    }

    public GraduationLineView getCompletion() {
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
        if (!(o instanceof ProgramView)) {
            return false;
        }
        ProgramView that = (ProgramView) o;
        return code.equals(that.code)
                && title.equals(that.title)
                && status.equals(that.status)
                && enrolment.equals(that.enrolment)
                && completion.equals(that.completion)
                && notes.equals(that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, status, enrolment, completion, notes);
    }

    @Override
    public String toString() {
        return "ProgramView{" + code + " \"" + title + "\", " + status + ", notes=" + notes + "}";
    }
}
