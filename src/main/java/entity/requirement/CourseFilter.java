package entity.requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The course-matching fields that {@code credits} and {@code average} nodes share, as described in "Course filters"
 * in the backend README. This class doesn't match courses itself.
 *
 * <p>A course passes the {@code courses} and {@code designators} check if it's in {@code courses} or has one of
 * {@code designators}. The other fields narrow the match.
 */
public final class CourseFilter {
    private final List<String> courses;
    private final List<String> designators;
    private final List<Integer> levels;
    private final List<Integer> breadth;
    private final List<String> lists;
    private final List<Integer> requirements;
    private final List<String> exclude;
    private final List<String> excludeDesignators;
    private final Double maxCourseAmount;
    private final Integer minGrade;
    private final boolean transfer;
    private final boolean crNcr;

    private CourseFilter(Builder b) {
        this.courses = List.copyOf(b.courses);
        this.designators = List.copyOf(b.designators);
        this.levels = List.copyOf(b.levels);
        this.breadth = List.copyOf(b.breadth);
        this.lists = List.copyOf(b.lists);
        this.requirements = List.copyOf(b.requirements);
        this.exclude = List.copyOf(b.exclude);
        this.excludeDesignators = List.copyOf(b.excludeDesignators);
        this.maxCourseAmount = b.maxCourseAmount;
        this.minGrade = b.minGrade;
        this.transfer = b.transfer;
        this.crNcr = b.crNcr;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Returns a filter that matches every course. */
    public static CourseFilter any() {
        return builder().build();
    }

    /**
     * Returns whether {@code courses} and {@code designators} are both empty. The other fields can still narrow the
     * match.
     */
    public boolean matchesAnyCourse() { return courses.isEmpty() && designators.isEmpty(); }

    public List<String> getCourses() { return courses; }
    public List<String> getDesignators() { return designators; }
    public List<Integer> getLevels() { return levels; }
    /** Returns breadth category numbers, 1-5. */
    public List<Integer> getBreadth() { return breadth; }
    /** Returns names of course lists in the program's completion. A course matches if it's on any of them. */
    public List<String> getLists() { return lists; }
    /** Returns completion row numbers. A course matches if it counts toward any of them. */
    public List<Integer> getRequirements() { return requirements; }
    public List<String> getExclude() { return exclude; }
    public List<String> getExcludeDesignators() { return excludeDesignators; }
    /** Returns the {@code max_course_amount} cap, or empty for no cap. */
    public Optional<Double> getMaxCourseAmount() { return Optional.ofNullable(maxCourseAmount); }
    /** Returns the minimum percent each matching course needs. Only {@code credits} nodes set it. */
    public Optional<Integer> getMinGrade() { return Optional.ofNullable(minGrade); }
    /** Returns whether only transfer credits match. */
    public boolean isTransfer() { return transfer; }
    /** Returns whether only courses taken Credit/No Credit match. */
    public boolean isCrNcr() { return crNcr; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseFilter)) {
            return false;
        }
        CourseFilter that = (CourseFilter) o;
        return transfer == that.transfer
                && crNcr == that.crNcr
                && courses.equals(that.courses)
                && designators.equals(that.designators)
                && levels.equals(that.levels)
                && breadth.equals(that.breadth)
                && lists.equals(that.lists)
                && requirements.equals(that.requirements)
                && exclude.equals(that.exclude)
                && excludeDesignators.equals(that.excludeDesignators)
                && Objects.equals(maxCourseAmount, that.maxCourseAmount)
                && Objects.equals(minGrade, that.minGrade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courses, designators, levels, breadth, lists, requirements,
                exclude, excludeDesignators, maxCourseAmount, minGrade, transfer, crNcr);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CourseFilter{");
        append(sb, "courses", courses);
        append(sb, "designators", designators);
        append(sb, "levels", levels);
        append(sb, "breadth", breadth);
        append(sb, "lists", lists);
        append(sb, "requirements", requirements);
        append(sb, "exclude", exclude);
        append(sb, "excludeDesignators", excludeDesignators);
        if (maxCourseAmount != null) {
            appendField(sb, "maxCourseAmount", maxCourseAmount);
        }
        if (minGrade != null) {
            appendField(sb, "minGrade", minGrade);
        }
        if (transfer) {
            appendField(sb, "transfer", true);
        }
        if (crNcr) {
            appendField(sb, "crNcr", true);
        }
        return sb.append('}').toString();
    }

    private static void append(StringBuilder sb, String name, List<?> values) {
        if (!values.isEmpty()) {
            appendField(sb, name, values);
        }
    }

    private static void appendField(StringBuilder sb, String name, Object value) {
        if (sb.charAt(sb.length() - 1) != '{') {
            sb.append(", ");
        }
        sb.append(name).append('=').append(value);
    }

    public static final class Builder {
        private List<String> courses = List.of();
        private List<String> designators = List.of();
        private List<Integer> levels = List.of();
        private List<Integer> breadth = List.of();
        private List<String> lists = List.of();
        private List<Integer> requirements = List.of();
        private List<String> exclude = List.of();
        private List<String> excludeDesignators = List.of();
        private Double maxCourseAmount = null;
        private Integer minGrade = null;
        private boolean transfer = false;
        private boolean crNcr = false;

        private Builder() {}

        public Builder courses(List<String> v) { this.courses = Objects.requireNonNull(v); return this; };
        public Builder designators(List<String> v) { this.designators = Objects.requireNonNull(v); return this; };
        public Builder levels(List<Integer> v) { this.levels = Objects.requireNonNull(v); return this; };
        public Builder breadth(List<Integer> v) { this.breadth = Objects.requireNonNull(v); return this; };
        public Builder lists(List<String> v) { this.lists = Objects.requireNonNull(v); return this; };
        public Builder requirements(List<Integer> v) { this.requirements = Objects.requireNonNull(v); return this; };
        public Builder exclude(List<String> v) { this.exclude = Objects.requireNonNull(v); return this; };
        public Builder excludeDesignators(List<String> v) { this.excludeDesignators = Objects.requireNonNull(v); return this; };
        public Builder maxCourseAmount(Double v) { this.maxCourseAmount = Objects.requireNonNull(v); return this; };
        public Builder minGrade(Integer v) { this.minGrade = Objects.requireNonNull(v); return this; };
        public Builder transfer(boolean v) { this.transfer = v; return this; };
        public Builder crNcr(boolean v) { this.crNcr = v; return this; };

        /**
         * @throws IllegalArgumentException if {@code maxCourseAmount} is negative or {@code minGrade} is outside 0-100
         */
        public CourseFilter build() throws IllegalArgumentException {
            if (maxCourseAmount != null && (!Double.isFinite(maxCourseAmount) || maxCourseAmount < 0)) {
                throw new IllegalArgumentException("maxCourseAmount must be >= 0");
            }
            if (minGrade != null && (minGrade < 0 || minGrade > 100)) {
                throw new IllegalArgumentException("minGrade must be 0-100");
            }
            return new CourseFilter(this);
        }
    }
}
