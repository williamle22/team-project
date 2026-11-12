package entity.timetable;

import entity.term.AcademicTerm;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

public final class MeetingTime {
    private final AcademicTerm term;
    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;
    private final String location;

    public MeetingTime(AcademicTerm term, DayOfWeek day, LocalTime start, LocalTime end, String location) {
        this.term = Objects.requireNonNull(term, "term");
        this.day = Objects.requireNonNull(day, "day");
        this.start = Objects.requireNonNull(start, "start");
        this.end = Objects.requireNonNull(end, "end");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start must be before end, was " + start + "-" + end);
        }
        this.location = location;
    }

    public AcademicTerm getTerm() {
        return term;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStart() {
        return start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    public boolean overlaps(MeetingTime other) {
        return term.equals(other.term) && day == other.day
                && start.isBefore(other.end) && other.start.isBefore(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MeetingTime)) {
            return false;
        }
        MeetingTime that = (MeetingTime) o;
        return term.equals(that.term) && day == that.day && start.equals(that.start) && end.equals(that.end)
                && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(term, day, start, end, location);
    }

    @Override
    public String toString() {
        return "MeetingTime{" + term + ", " + day + " " + start + "-" + end
                + (location == null ? "" : ", " + location) + "}";
    }
}
