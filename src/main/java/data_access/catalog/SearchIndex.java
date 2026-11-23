package data_access.catalog;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public final class SearchIndex<T> {
    private final List<Entry<T>> entries;

    public SearchIndex(Collection<T> items, Function<T, String> code, Function<T, String> title) {
        List<Entry<T>> e = new ArrayList<>();

        for (T item : items) {
            e.add(new Entry<>(item, code.apply(item), title.apply(item)));
        }

        this.entries = e;
    }

    public static <T> SearchIndex<T> empty() {
        return new SearchIndex<>(List.of(), t -> "", t -> "");
    }

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
