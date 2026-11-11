package entity.catalog;

import entity.requirement.ProfileReq;
import entity.requirement.Requirement;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CompletionRow {
    private final int number;
    private final RowKind kind;
    private final String label;
    private final String section;
    private final Integer parent;
    private final List<Integer> overlaps;
    private final Requirement req;
    private final ProfileReq audience;
    private final boolean transcriptScope;
    private final List<String> notes;
    private final String raw;

    public CompletionRow(int number, RowKind kind, String label, String section, Integer parent,
                         List<Integer> overlaps, Requirement req, ProfileReq audience,
                         boolean transcriptScope, List<String> notes, String raw) {
        if (number < 1) {
            throw new IllegalArgumentException("number must be >= 1, was " + number);
        }
        this.number = number;
        this.kind = Objects.requireNonNull(kind, "kind");
        this.label = label;
        this.section = section;
        this.parent = parent;
        this.overlaps = List.copyOf(Objects.requireNonNull(overlaps, "overlaps"));
        this.req = req;
        this.audience = audience;
        this.transcriptScope = transcriptScope;
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
        this.raw = Objects.requireNonNull(raw, "raw");
    }

    public int getNumber() {
        return number;
    }

    public RowKind getKind() {
        return kind;
    }

    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    public Optional<String> getSection() {
        return Optional.ofNullable(section);
    }

    public Optional<Integer> getParent() {
        return Optional.ofNullable(parent);
    }

    public boolean isTopLevel() {
        return parent == null;
    }

    public List<Integer> getOverlaps() {
        return overlaps;
    }

    public Optional<Requirement> getReq() {
        return Optional.ofNullable(req);
    }

    public boolean isOptionGroup() {
        return req == null;
    }

    public Optional<ProfileReq> getAudience() {
        return Optional.ofNullable(audience);
    }

    public boolean isTranscriptScope() {
        return transcriptScope;
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
        if (!(o instanceof CompletionRow)) {
            return false;
        }
        CompletionRow that = (CompletionRow) o;
        return number == that.number
                && kind == that.kind
                && Objects.equals(label, that.label)
                && Objects.equals(section, that.section)
                && Objects.equals(parent, that.parent)
                && overlaps.equals(that.overlaps)
                && Objects.equals(req, that.req)
                && Objects.equals(audience, that.audience)
                && transcriptScope == that.transcriptScope
                && notes.equals(that.notes)
                && raw.equals(that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, kind, label, section, parent, overlaps, req, audience, transcriptScope, notes, raw);
    }

    @Override
    public String toString() {
        return "CompletionRow{number=" + number + ", kind=" + kind + ", label=" + label + ", parent=" + parent
                + ", req=" + req + "}";
    }
}
