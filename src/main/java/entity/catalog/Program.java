package entity.catalog;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One program from the Faculty of Arts and Science calendar ({@code programs} table), such as {@code ASMIN1001}.
 * Equality uses only {@code code}.
 */
public final class Program {
    private final String code;
    private final ProgramType type;
    private final String number;
    private final String title;
    private final Enrolment enrolment;
    private final Completion completion;
    private final List<String> areas;

    /**
     * Creates a program from the columns of the {@code programs} table, in column order. {@code enrolment} and {@code completion} are {@code null} when
     * their column is {@code NULL}.
     */
    public Program(String code, ProgramType type, String number, String title,
                   Enrolment enrolment, Completion completion, List<String> areas) {
        this.code = Objects.requireNonNull(code, "code");
        this.type = Objects.requireNonNull(type, "type");
        this.number = Objects.requireNonNull(number, "number");
        this.title = Objects.requireNonNull(title, "title");
        this.enrolment = enrolment;
        this.completion = completion;
        this.areas = List.copyOf(Objects.requireNonNull(areas, "areas"));
    }

    public String getCode() {
        return code;
    }

    public ProgramType getType() {
        return type;
    }

    /** Returns the code after its 5-character prefix, such as {@code "1001"} or {@code "1469A"}. */
    public String getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public Optional<Enrolment> getEnrolment() {
        return Optional.ofNullable(enrolment);
    }

    public Optional<Completion> getCompletion() {
        return Optional.ofNullable(completion);
    }

    /** Returns the subject areas the program belongs to. */
    public List<String> getAreas() {
        return areas;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Program)) {
            return false;
        }
        return code.equals(((Program) o).code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    @Override
    public String toString() {
        return "Program{" + code + ", " + title + "}";
    }
}
