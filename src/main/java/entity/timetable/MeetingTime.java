package entity.timetable;

import entity.term.AcademicTerm;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

/**
 * One weekly meeting of a section in one term. Each meeting has its own term because EASI lists a full-year (Y)
 * course's meetings separately for Fall and Winter, and the room can differ between terms.
 */
public final class MeetingTime {
    private final AcademicTerm term;
    private final DayOfWeek day;
    private final LocalTime start;
    private final LocalTime end;
    private final String location;

    /**
     * Creates a meeting. {@code location} is {@code null} when EASI gives no room.
     *
     * @throws IllegalArgumentException if {@code start} isn't before {@code end}
     */
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

    /** Returns the room, or empty when EASI gives no room. */
    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     * Returns whether this meeting and {@code other} are in the same term and on the same day, with intersecting
     * times. Back-to-back meetings, such as one that ends at 11:00 and one that starts at 11:00, don't overlap.
     */
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
