package use_case;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

/** Ranks search results by how closely their code matches the query. Both of Use Case 2's searches use it. */
public final class CodeRanking {
    private CodeRanking() {}

    /**
     * Returns a comparator that puts an exact code match first, then codes that start with the query, then codes that
     * contain it, then everything else. Items with the same rank sort by code. It removes the query's spaces and
     * uppercases it, so {@code "csc 148h1"} is an exact match for {@code CSC148H1}.
     */
    public static <T> Comparator<T> by(String query, Function<T, String> code) {
        String q = query.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);

        return Comparator.comparingInt((T item) -> rank(code.apply(item), q)).thenComparing(code);
    }

    private static int rank(String code, String q) {
        if (code.equals(q)) {
            return 0;
        }

        if (code.startsWith(q)) {
            return 1;
        }

        if (code.contains(q)) {
            return 2;
        }
        return 3;
    }
}
