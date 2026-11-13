package entity.term;

import entity.catalog.Section;
import entity.catalog.SessionPeriod;

import java.util.List;
import java.util.Objects;

/**
 * When a course runs: a session period, a year, and a section.
 *
 * <p>For {@code FALL_WINTER}, {@code year} is the year of the Fall term, so {@code fallWinter(2026, S)} is Winter
 * 2027. For {@code SUMMER}, {@code year} is the year of the summer.
 */
public final class Placement {
    private final SessionPeriod period;
    private final int year;
    private final Section section;

    public Placement(SessionPeriod period, int year, Section section) {
        this.period = Objects.requireNonNull(period, "period");
        this.year = year;
        this.section = Objects.requireNonNull(section, "section");
    }

    public static Placement fallWinter(int fallYear, Section section) {
        return new Placement(SessionPeriod.FALL_WINTER, fallYear, section);
    }

    public static Placement summer(int year, Section section) {
        return new Placement(SessionPeriod.SUMMER, year, section);
    }

    /**
     * Returns the placement of a half course that runs in {@code term}.
     *
     * @throws IllegalArgumentException if {@code term} is a summer term, because a summer half course needs an
     *     explicit section ({@code F} or {@code S})
     */
    public static Placement halfCourseIn(AcademicTerm term) {
        switch (term.getSeason()) {
            case FALL:
                return fallWinter(term.getYear(), Section.F);
            case WINTER:
                return fallWinter(term.getYear() - 1, Section.S);
            default:
                throw new IllegalArgumentException("a summer placement needs an explicit section");
        }
    }

    public SessionPeriod getPeriod() {
        return period;
    }

    public int getYear() {
        return year;
    }

    public Section getSection() {
        return section;
    }

    public boolean isSummer() {
        return period == SessionPeriod.SUMMER;
    }

    public AcademicTerm startTerm() {
        if (isSummer()) {
            return AcademicTerm.summer(year);
        }
        return section == Section.S ? AcademicTerm.winter(year + 1) : AcademicTerm.fall(year);
    }

    public AcademicTerm endTerm() {
        if (isSummer()) {
            return AcademicTerm.summer(year);
        }
        return section == Section.F ? AcademicTerm.fall(year) : AcademicTerm.winter(year + 1);
    }

    /** Returns the one or two terms this placement occupies, in calendar order. */
    public List<AcademicTerm> terms() {
        AcademicTerm start = startTerm();
        AcademicTerm end = endTerm();
        return start.equals(end) ? List.of(start) : List.of(start, end);
    }

    public boolean occupies(AcademicTerm term) {
        return !term.isBefore(startTerm()) && !term.isAfter(endTerm());
    }

    /**
     * Returns the academic year of this placement, which is the year its Fall term starts. For example, Summer 2027
     * belongs to 2026.
     */
    public int academicYear() {
        return isSummer() ? year - 1 : year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Placement)) {
            return false;
        }
        Placement that = (Placement) o;
        return period == that.period && year == that.year && section == that.section;
    }

    @Override
    public int hashCode() {
        return Objects.hash(period, year, section);
    }

    @Override
    public String toString() {
        if (isSummer()) {
            return "Summer " + year + " (" + section.getValue() + ")";
        }
        if (section == Section.Y) {
            return "Fall-Winter " + year + "-" + (year + 1) + " (Y)";
        }
        return startTerm().toString();
    }
}
