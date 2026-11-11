package entity.catalog;

import java.util.List;
import java.util.Objects;

/**
 * The exclusions of a course ({@code exclusions} column): items that block taking the course. {@link #getRaw()}
 * can be empty when the items come from a timing restriction in the requisites.
 */
public final class Exclusions {
    private final List<ExclusionItem> items;
    private final List<String> notes;
    private final String raw;

    public Exclusions(List<ExclusionItem> items, List<String> notes, String raw) {
        this.items = List.copyOf(Objects.requireNonNull(items, "items"));
        this.notes = List.copyOf(Objects.requireNonNull(notes, "notes"));
        this.raw = Objects.requireNonNull(raw, "raw");
    }

    public List<ExclusionItem> getItems() {
        return items;
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
        if (!(o instanceof Exclusions)) {
            return false;
        }
        Exclusions that = (Exclusions) o;
        return items.equals(that.items) && notes.equals(that.notes) && raw.equals(that.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, notes, raw);
    }

    @Override
    public String toString() {
        return "Exclusions{items=" + items + ", notes=" + notes + ", raw='" + raw + "'}";
    }
}