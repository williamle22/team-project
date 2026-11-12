package entity.degree;

import entity.catalog.ProgramType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ProgramCombinationRule {
    private final List<Map<ProgramType, Integer>> combinations;

    public ProgramCombinationRule(List<Map<ProgramType, Integer>> combinations) {
        Objects.requireNonNull(combinations, "combinations");
        if (combinations.isEmpty()) {
            throw new IllegalArgumentException("combinations must not be empty");
        }
        for (Map<ProgramType, Integer> c : combinations) {
            if (c.isEmpty() || c.values().stream().anyMatch(n -> n < 1)) {
                throw new IllegalArgumentException("each combination needs counts >= 1, was " + c);
            }
        }
        this.combinations = combinations.stream().map(Map::copyOf).collect(Collectors.toUnmodifiableList());
    }

    public List<Map<ProgramType, Integer>> getCombinations() {
        return combinations;
    }

    public boolean satisfiedBy(Collection<ProgramType> programs) {
        Map<ProgramType, Integer> counts = new EnumMap<>(ProgramType.class);
        for (ProgramType type : programs) {
            counts.merge(type, 1, Integer::sum);
        }
        return combinations.stream().anyMatch(c -> c.entrySet().stream()
                .allMatch(e -> counts.getOrDefault(e.getKey(), 0) >= e.getValue()));
    }

    public String describe() {
        List<String> options = new ArrayList<>();
        for (Map<ProgramType, Integer> c : combinations) {
            List<String> parts = new ArrayList<>();
            for (ProgramType type : ProgramType.values()) {
                Integer n = c.get(type);
                if (n != null) {
                    parts.add(n + " " + plural(type.getValue(), n));
                }
            }
            options.add(String.join(" and ", parts));
        }
        return String.join(", or ", options);
    }

    private static String plural(String word, int n) {
        if (n == 1) {
            return word;
        }
        return word.endsWith("s") ? word + "es" : word + "s";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgramCombinationRule)) {
            return false;
        }
        ProgramCombinationRule that = (ProgramCombinationRule) o;
        return combinations.equals(that.combinations);
    }

    @Override
    public int hashCode() {
        return combinations.hashCode();
    }

    @Override
    public String toString() {
        return "ProgramCombinationRule{" + describe() + "}";
    }
}
