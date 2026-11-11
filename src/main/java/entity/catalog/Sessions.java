package entity.catalog;

import java.util.List;
import java.util.Objects;

/**
 * The sections a course is offered in ({@code sessions} column), grouped by {@link SessionPeriod}.
 *
 * <p>{@code Sessions} describes only the current academic year. A course with no {@code Sessions} isn't offered
 * this year. It says nothing about any other year.
 */
public final class Sessions {
    private final List<Section> fallWinter;
    private final List<Section> summer;

    public Sessions(List<Section> fallWinter, List<Section> summer) {
        this.fallWinter = List.copyOf(Objects.requireNonNull(fallWinter, "fallWinter"));
        this.summer = List.copyOf(Objects.requireNonNull(summer, "summer"));
    }

    public List<Section> getFallWinter() {
        return fallWinter;
    }

    public List<Section> getSummer() {
        return summer;
    }

    public List<Section> sections(SessionPeriod period) {
        return period == SessionPeriod.SUMMER ? summer : fallWinter;
    }

    /**
     * Returns whether {@code period} lists {@code section}. A {@code Y} placement needs {@code Y},
     * not {@code F} and {@code S}.
     */
    public boolean offers(SessionPeriod period, Section section) {
        return sections(period).contains(section);
    }

    /** Returns whether the fall-winter sections include {@code F} or {@code Y}. */
    public boolean offeredInFall() {
        return this.fallWinter.contains(Section.F) || fallWinter.contains(Section.Y);
    }

    /** Returns whether the fall-winter sections include {@code S} or {@code Y}. */
    public boolean offeredInWinter() {
        return this.fallWinter.contains(Section.S) || fallWinter.contains(Section.Y);
    }

    public boolean offeredInSummer() {
        return !summer.isEmpty();
    }

    /** Returns whether the fall-winter sections include {@code Y}. */
    public boolean isFullYear() {
        return this.fallWinter.contains(Section.Y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sessions)) {
            return false;
        }
        Sessions that = (Sessions) o;
        return fallWinter.equals(that.fallWinter) && summer.equals(that.summer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fallWinter, summer);
    }

    @Override
    public String toString() {
        return "Sessions{fallWinter=" + fallWinter + ", summer=" + summer + "}";
    }
}