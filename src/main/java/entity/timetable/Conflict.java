package entity.timetable;

import entity.term.AcademicTerm;

import java.util.Objects;

/** Two chosen meetings in the same term whose times overlap. */
public final class Conflict {
    private final ScheduledMeeting first;
    private final ScheduledMeeting second;

    public Conflict(ScheduledMeeting first, ScheduledMeeting second) {
        this.first = Objects.requireNonNull(first, "first");
        this.second = Objects.requireNonNull(second, "second");
    }

    /** Returns the term both meetings are in. */
    public AcademicTerm getTerm() {
        return first.getTime().getTerm();
    }

    public ScheduledMeeting getFirst() {
        return first;
    }

    public ScheduledMeeting getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Conflict)) {
            return false;
        }
        Conflict that = (Conflict) o;
        return first.equals(that.first) && second.equals(that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "Conflict{" + first + ", " + second + "}";
    }
}
