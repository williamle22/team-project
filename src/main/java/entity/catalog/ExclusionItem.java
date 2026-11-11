package entity.catalog;

import entity.requirement.TextKind;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One exclusion of a course. Each item has exactly one of the following shapes:
 * <ul>
 *   <li>Text ({@link #isText()}): {@code text} is set, with a {@code kind}, and needs manual review.
 *   <li>Course set ({@link #isCourseSet()}): having taken any of {@code courses} excludes the course.
 *   <li>Filter ({@link #isFilter()}): {@code designators}, {@code levels}, and {@code exclude} narrow which courses
 *       exclude the course.
 * </ul>
 */
public final class ExclusionItem {
    private final List<String> courses;
    private final List<String> designators;
    private final List<Integer> levels;
    private final List<String> exclude;
    private final boolean concurrent;
    private final String note;
    private final TextKind kind;
    private final String text;

    public ExclusionItem(List<String> courses, List<String> designators, List<Integer> levels,
                         List<String> exclude, boolean concurrent, String note, TextKind kind, String text) {
        this.courses = List.copyOf(Objects.requireNonNull(courses, "courses"));
        this.designators = List.copyOf(Objects.requireNonNull(designators, "designators"));
        this.levels = List.copyOf(Objects.requireNonNull(levels, "levels"));
        this.exclude = List.copyOf(Objects.requireNonNull(exclude, "exclude"));
        this.concurrent = concurrent;
        this.note = note;
        this.kind = kind;
        this.text = text;
    }

    public List<String> getCourses() {
        return courses;
    }

    public List<String> getDesignators() {
        return designators;
    }

    public List<Integer> getLevels() {
        return levels;
    }

    public List<String> getExclude() {
        return exclude;
    }

    /** Returns whether taking a matching course at the same time also excludes the course. */
    public boolean isConcurrent() {
        return concurrent;
    }

    /** Returns the calendar's qualifier for this exclusion. */
    public Optional<String> getNote() {
        return Optional.ofNullable(note);
    }

    public Optional<TextKind> getKind() {
        return Optional.ofNullable(kind);
    }

    public Optional<String> getText() {
        return Optional.ofNullable(text);
    }

    public boolean isText() {
        return text != null;
    }

    public boolean isCourseSet() {
        return !isText() && !courses.isEmpty();
    }

    public boolean isFilter() {
        return !isText() && !isCourseSet();
    }

    public boolean hasNote() {
        return note != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExclusionItem)) {
            return false;
        }
        ExclusionItem that = (ExclusionItem) o;
        return concurrent == that.concurrent
                && courses.equals(that.courses)
                && designators.equals(that.designators)
                && levels.equals(that.levels)
                && exclude.equals(that.exclude)
                && Objects.equals(note, that.note)
                && kind == that.kind
                && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courses, designators, levels, exclude, concurrent, note, kind, text);
    }

    @Override
    public String toString() {
        return "ExclusionItem{courses=" + courses + ", designators=" + designators + ", levels=" + levels
                + ", exclude=" + exclude + ", concurrent=" + concurrent + ", note=" + note
                + ", kind=" + kind + ", text=" + text + "}";
    }
}