package use_case.check_graduation;

import entity.evaluation.Status;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The result of checking one plan. {@code degree} is the degree's line, with its rows as children, and
 * {@code combination} is the program combination's line. {@code programs} is in the plan's program order, and
 * {@code notes} apply to the whole plan.
 */
public final class CheckGraduationOutputData {
    private final UUID planId;
    private final boolean projected;
    private final Status overall;
    private final RowLine degree;
    private final RowLine combination;
    private final List<String> notes;
    private final List<ProgramSummary> programs;

    public CheckGraduationOutputData(UUID planId, boolean projected, Status overall, RowLine degree,
                                     RowLine combination, List<String> notes, List<ProgramSummary> programs) {
        this.planId = Objects.requireNonNull(planId, "planId");
        this.projected = projected;
        this.overall = Objects.requireNonNull(overall, "overall");
        this.degree = Objects.requireNonNull(degree, "degree");
        this.combination = Objects.requireNonNull(combination, "combination");
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
        this.programs = List.copyOf(Objects.requireNonNull(programs, "programs"));
    }

    public UUID getPlanId() {
        return planId;
    }

    public boolean isProjected() {
        return projected;
    }

    public Status getOverall() {
        return overall;
    }

    public RowLine getDegree() {
        return degree;
    }

    public RowLine getCombination() {
        return combination;
    }

    public List<String> getNotes() {
        return notes;
    }

    public List<ProgramSummary> getPrograms() {
        return programs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CheckGraduationOutputData)) {
            return false;
        }
        CheckGraduationOutputData that = (CheckGraduationOutputData) o;
        return planId.equals(that.planId)
                && projected == that.projected
                && overall == that.overall
                && degree.equals(that.degree)
                && combination.equals(that.combination)
                && notes.equals(that.notes)
                && programs.equals(that.programs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, projected, overall, degree, combination, notes, programs);
    }

    @Override
    public String toString() {
        return "CheckGraduationOutputData{" + planId + (projected ? ", whole plan" : ", completed only") + ", "
                + overall + ", degree=" + degree.getStatus() + ", combination=" + combination.getStatus()
                + ", programs=" + programs + ", notes=" + notes + "}";
    }
}
