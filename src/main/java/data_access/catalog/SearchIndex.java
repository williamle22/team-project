package data_access.catalog;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Finds items by code or by title words in a fixed list of items, such as courses or programs. It's immutable, so you
 * can search it from any thread while another thread builds a new index.
 */
public final class SearchIndex<T> {
    private final List<Entry<T>> entries;

    /** Creates an index of {@code items}, reading each item's code and title with {@code code} and {@code title}. */
    public SearchIndex(Collection<T> items, Function<T, String> code, Function<T, String> title) {
        List<Entry<T>> e = new ArrayList<>();

        for (T item : items) {
            e.add(new Entry<>(item, code.apply(item), title.apply(item)));
        }

        this.entries = e;
    }

    /** Returns an index with no items, for use before the first catalog loads. */
    public static <T> SearchIndex<T> empty() {
        return new SearchIndex<>(List.of(), t -> "", t -> "");
    }

    /**
     * Returns the items that match {@code query} and pass {@code filter}, in the order the constructor received them.
     * An item matches if its code contains the query with all spaces removed, so {@code "csc 148"} finds
     * {@code CSC148H1}. It also matches if its code and title contain every word of the query. A blank query matches
     * every item.
     */
    public List<T> search(String query, Predicate<T> filter) {
        String stripped = query.strip();
        String compact = stripped.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        String[] words = stripped.toLowerCase(Locale.ROOT).split("\\s+");

        List<T> results = new ArrayList<>();

        for (Entry<T> e : entries) {
            if ((stripped.isBlank() || e.codeKey.contains(compact) || Arrays.stream(words).allMatch(e.text::contains))
                    && filter.test(e.item)) {
                results.add(e.item);
            }
        }

        return results;
    }

    private static final class Entry<T> {
        private final T item;
        private final String codeKey;
        private final String text;

        Entry(T item, String code, String title) {
            this.item = item;
            this.codeKey = code;
            this.text = (code + " " + title).toLowerCase(Locale.ROOT);
        }
    }
}
