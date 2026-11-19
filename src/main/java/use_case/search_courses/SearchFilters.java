package use_case.search_courses;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import entity.catalog.Course;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Filters that narrow a search. An empty set, or a missing designator, accepts any value. */
public final class SearchFilters {
    private final String designator;
    private final Set<Integer> levels;
    private final Set<Campus> campuses;
    private final Set<BreadthCategory> breadth;

    /**
     * Creates filters. A blank designator means any designator. Otherwise the constructor strips and uppercases it, so
     * {@code "csc"} matches {@code CSC}.
     *
     * @throws NullPointerException if a set, or any element of a set, is {@code null}
     */
    public SearchFilters(String designator, Set<Integer> levels, Set<Campus> campuses, Set<BreadthCategory> breadth) {
        if (designator == null || designator.isBlank()) {
            this.designator = null;
        } else {
            this.designator = designator.strip().toUpperCase(Locale.ROOT);
        }

        this.levels = Set.copyOf(levels);
        this.campuses = Set.copyOf(campuses);
        this.breadth = Set.copyOf(breadth);
    }

    /** Returns filters that accept every course. */
    public static SearchFilters none() {
        return new SearchFilters(null, Set.of(), Set.of(), Set.of());
    }

    public Optional<String> getDesignator() {
        return Optional.ofNullable(designator);
    }

    public Set<Integer> getLevels() {
        return levels;
    }

    public Set<Campus> getCampuses() {
        return campuses;
    }

    public Set<BreadthCategory> getBreadth() {
        return breadth;
    }

    /**
     * Returns whether {@code course} passes every filter. The breadth filter passes if the course has any of the
     * chosen categories.
     */
    public boolean accepts(Course course) {
        return (designator == null || course.getDesignator().equals(designator))
                && (levels.isEmpty() || levels.contains(course.getLevel()))
                && (campuses.isEmpty() || campuses.contains(course.getCampus()))
                && (breadth.isEmpty() || course.getBreadth().stream().anyMatch(breadth::contains));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchFilters)) {
            return false;
        }
        SearchFilters that = (SearchFilters) o;
        return Objects.equals(designator, that.designator) && levels.equals(that.levels)
                && campuses.equals(that.campuses) && breadth.equals(that.breadth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(designator, levels, campuses, breadth);
    }

    @Override
    public String toString() {
        return "SearchFilters{" + designator + ", levels " + levels + ", campuses " + campuses + ", breadth "
                + breadth + "}";
    }
}
