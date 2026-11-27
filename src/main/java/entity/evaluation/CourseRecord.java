package entity.evaluation;

import entity.catalog.Campus;
import entity.catalog.Course;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One course that the student has taken or is taking. {@code grade} is a percentage (0-100), or {@code null} if there's
 * no final grade yet. Transfer credits have no grade.
 */
public final class CourseRecord {
    private final Course course;
    private final Integer grade;
    private final boolean transfer;
    private final boolean crNcr;
    private final double weight;

    public CourseRecord(Course course, Integer grade, boolean transfer, boolean crNcr) {
        this(course, grade, transfer, crNcr, Objects.requireNonNull(course, "course").getWeight());
    }

    private CourseRecord(Course course, Integer grade, boolean transfer, boolean crNcr, double weight) {
        this.course = Objects.requireNonNull(course, "course");
        this.grade = grade;
        this.transfer = transfer;
        this.crNcr = crNcr;
        this.weight = weight;
    }

    /**
     * Returns a record for an unspecified transfer credit such as {@code MAT1**}, or empty if {@code code} isn't in
     * that form. Its course is a placeholder with the code's designator and level.
     */
    public static Optional<CourseRecord> unspecifiedTransfer(String code, double weight) {
        if (!code.matches("[A-Z]{3}[1-4]\\*+")) {
            return Optional.empty();
        }

        Course course = new Course(code, code.substring(0, 3), code.substring(3), (code.charAt(3) - '0') * 100, 0.5,
                Campus.UTSG, "Unspecified transfer credit", "", null, null, null, List.of(), null);

        return Optional.of(new CourseRecord(course, null, true, false, weight));
    }

    public Course getCourse() {
        return course;
    }

    public String getCode() {
        return course.getCode();
    }

    public Optional<Integer> getGrade() {
        return Optional.ofNullable(grade);
    }

    public boolean isTransfer() {
        return transfer;
    }

    public boolean isCrNcr() {
        return crNcr;
    }

    /**
     * Returns the credit weight. For an unspecified transfer credit, this is the plan's weight, not the placeholder
     * course's.
     */
    public double getWeight() {
        return weight;
    }

    public boolean isUnspecified() {
        return getCode().contains("*");
    }

    /** Returns whether this unspecified credit could be {@code code}, as {@code MAT1**} could be {@code MAT135H1}. */
    public boolean couldBe(String code) {
        return isUnspecified() && code.startsWith(getCode().substring(0, getCode().indexOf('*')));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseRecord)) {
            return false;
        }
        CourseRecord that = (CourseRecord) o;
        return course.equals(that.course)
                && Objects.equals(grade, that.grade)
                && transfer == that.transfer
                && crNcr == that.crNcr
                && Double.compare(weight, that.weight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, grade, transfer, crNcr, weight);
    }

    @Override
    public String toString() {
        return "CourseRecord{" + getCode() + ", grade=" + grade + ", weight=" + weight + (transfer ? ", transfer" : "")
                + (crNcr ? ", CR/NCR" : "") + "}";
    }
}
