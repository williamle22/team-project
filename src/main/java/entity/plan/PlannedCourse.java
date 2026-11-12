package entity.plan;

import entity.grades.CourseGrade;
import entity.term.Placement;
import entity.timetable.TimetableChoice;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlannedCourse {
    private final UUID id;
    private final String code;
    private final Placement placement;
    private final boolean crNcr;
    private final CourseGrade grade;
    private final TimetableChoice timetable;

    public PlannedCourse(UUID id, String code, Placement placement, boolean crNcr,
                         CourseGrade grade, TimetableChoice timetable) {
        this.id = Objects.requireNonNull(id, "id");
        this.code = Objects.requireNonNull(code, "code");
        this.placement = Objects.requireNonNull(placement, "placement");
        this.crNcr = crNcr;
        this.grade = Objects.requireNonNull(grade, "grade");
        this.timetable = timetable;
    }

    public static PlannedCourse create(String code, Placement placement) {
        return new PlannedCourse(UUID.randomUUID(), code, placement, false, CourseGrade.empty(), null);
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Placement getPlacement() {
        return placement;
    }

    public boolean isCrNcr() {
        return crNcr;
    }

    public CourseGrade getGrade() {
        return grade;
    }

    public Optional<TimetableChoice> getTimetable() {
        return Optional.ofNullable(timetable);
    }

    public PlannedCourse withPlacement(Placement placement) {
        return new PlannedCourse(id, code, placement, crNcr, grade, timetable);
    }

    public PlannedCourse withCrNcr(boolean crNcr) {
        return new PlannedCourse(id, code, placement, crNcr, grade, timetable);
    }

    public PlannedCourse withGrade(CourseGrade grade) {
        return new PlannedCourse(id, code, placement, crNcr, grade, timetable);
    }

    public PlannedCourse withTimetable(TimetableChoice timetable) {
        return new PlannedCourse(id, code, placement, crNcr, grade, timetable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlannedCourse)) {
            return false;
        }
        PlannedCourse that = (PlannedCourse) o;
        return id.equals(that.id) && code.equals(that.code) && placement.equals(that.placement)
                && crNcr == that.crNcr && grade.equals(that.grade) && Objects.equals(timetable, that.timetable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, placement, crNcr, grade, timetable);
    }

    @Override
    public String toString() {
        return "PlannedCourse{" + code + ", " + placement + (crNcr ? ", CR/NCR" : "") + ", " + id + "}";
    }
}
