package entity.evaluation;

import entity.catalog.Course;

import java.util.Objects;
import java.util.Optional;

public final class CourseRecord {
    private final Course course;
    private final Integer grade;
    private final boolean transfer;
    private final boolean crNcr;

    public CourseRecord(Course course, Integer grade, boolean transfer, boolean crNcr) {
        this.course = Objects.requireNonNull(course, "course");
        this.grade = grade;
        this.transfer = transfer;
        this.crNcr = crNcr;
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
                && crNcr == that.crNcr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(course, grade, transfer, crNcr);
    }

    @Override
    public String toString() {
        return "CourseRecord{" + getCode() + ", grade=" + grade + (transfer ? ", transfer" : "")
                + (crNcr ? ", CR/NCR" : "") + "}";
    }
}
