package entity.term;

import java.util.Objects;

public final class AcademicTerm implements Comparable<AcademicTerm> {
    private final Season season;
    private final int year;

    public AcademicTerm(Season season, int year) {
        this.season = Objects.requireNonNull(season, "season");
        this.year = year;
    }

    public static AcademicTerm fall(int year) {
        return new AcademicTerm(Season.FALL, year);
    }

    public static AcademicTerm winter(int year) {
        return new AcademicTerm(Season.WINTER, year);
    }

    public static AcademicTerm summer(int year) {
        return new AcademicTerm(Season.SUMMER, year);
    }

    public Season getSeason() {
        return season;
    }

    public int getYear() {
        return year;
    }

    public AcademicTerm next() {
        switch (season) {
            case WINTER:
                return summer(year);
            case SUMMER:
                return fall(year);
            default:
                return winter(year + 1);
        }
    }

    public AcademicTerm previous() {
        switch (season) {
            case FALL:
                return summer(year);
            case SUMMER:
                return winter(year);
            default:
                return fall(year - 1);
        }
    }

    public boolean isBefore(AcademicTerm other) {
        return compareTo(other) < 0;
    }

    public boolean isAfter(AcademicTerm other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(AcademicTerm other) {
        if (year != other.year) {
            return Integer.compare(year, other.year);
        }
        return season.compareTo(other.season);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcademicTerm)) {
            return false;
        }
        AcademicTerm that = (AcademicTerm) o;
        return season == that.season && year == that.year;
    }

    @Override
    public int hashCode() {
        return Objects.hash(season, year);
    }

    @Override
    public String toString() {
        String name = season.name();
        return name.charAt(0) + name.substring(1).toLowerCase() + " " + year;
    }
}
