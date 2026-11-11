package entity.catalog;

import entity.requirement.ProgramReq;
import entity.requirement.Requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** The enrolment rules of a program, from its {@code enrolment} JSON. */
public final class Enrolment {
    private final EnrolmentKind kind;
    private final Requirement req;
    private final List<ProgramReq> exclusions;
    private final boolean firstRoundOnly;
    private final List<String> notes;
    private final String raw;

    /**
     * @throws IllegalArgumentException if {@code req} is {@code null} for a kind other than {@code SUSPENDED}, or
     *     non-null for {@code SUSPENDED}
     */
    public Enrolment(EnrolmentKind kind, Requirement req, List<ProgramReq> exclusions,
                     boolean firstRoundOnly, List<String> notes, String raw) {
        this.kind = Objects.requireNonNull(kind, "kind");
        if ((kind == EnrolmentKind.SUSPENDED) != (req == null)) {
            throw new IllegalArgumentException("req must be null exactly when kind is SUSPENDED, was "
                    + kind + " with req " + req);
        }
        this.req = req;
        this.exclusions = List.copyOf(Objects.requireNonNull(exclusions, "exclusions"));
        this.firstRoundOnly = firstRoundOnly;
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
        this.raw = Objects.requireNonNull(raw, "raw");
    }

    public EnrolmentKind getKind() {
        return kind;
    }

    /** Returns the enrolment requirement, or empty when the kind is {@code SUSPENDED}. */
    public Optional<Requirement> getReq() {
        return Optional.ofNullable(req);
    }

    /** Returns the programs whose students can't enrol in this one. */
    public List<ProgramReq> getExclusions() {
        return exclusions;
    }

    /** Returns whether the program considers requests only in the first enrolment round. */
    public boolean isFirstRoundOnly() {
        return firstRoundOnly;
    }

    public List<String> getNotes() {
        return notes;
    }

    public String getRaw() {
        return raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Enrolment)) {
            return false;
        }
        Enrolment that = (Enrolment) o;
        return firstRoundOnly == that.firstRoundOnly
                && kind == that.kind
                && Objects.equals(req, that.req)
                && exclusions.equals(that.exclusions)
                && notes.equals(that.notes)
                && raw.equals(that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, req, exclusions, firstRoundOnly, notes, raw);
    }

    @Override
    public String toString() {
        return "Enrolment{kind=" + kind + ", req=" + req + ", exclusions=" + exclusions
                + ", firstRoundOnly=" + firstRoundOnly + "}";
    }
}