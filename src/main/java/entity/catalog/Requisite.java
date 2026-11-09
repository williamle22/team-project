package entity.catalog;

import entity.requirement.Requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Requisite {
    private final Requirement req;
    private final List<String> notes;
    private final String raw;

    public Requisite(Requirement req, List<String> notes, String raw) {
        this.req = req;
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
        this.raw = Objects.requireNonNull(raw, "raw");
    }

    public Optional<Requirement> getReq() {
        return Optional.ofNullable(req);
    }

    public boolean requiresNothing() {
        return req == null;
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
        if (!(o instanceof Requisite)) {
            return false;
        }
        Requisite that = (Requisite) o;
        return Objects.equals(req, that.req) && notes.equals(that.notes) && raw.equals(that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(req, notes, raw);
    }

    @Override
    public String toString() {
        return "Requisite{req=" + req + ", notes=" + notes + ", raw='" + raw + "'}";
    }
}