package entity.term;

import entity.catalog.Section;
import entity.catalog.SessionPeriod;

import java.util.List;
import java.util.Objects;

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

    public List<AcademicTerm> terms() {
        AcademicTerm start = startTerm();
        AcademicTerm end = endTerm();
        return start.equals(end) ? List.of(start) : List.of(start, end);
    }

    public boolean occupies(AcademicTerm term) {
        return !term.isBefore(startTerm()) && !term.isAfter(endTerm());
    }

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
