package use_case;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;

public final class CodeRanking {
    private CodeRanking() {}

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
