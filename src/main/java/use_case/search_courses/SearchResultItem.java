package use_case.search_courses;

import entity.catalog.Campus;

import java.util.List;
import java.util.Objects;

/** One search result, as plain values for the presenter. {@code breadth} holds breadth category numbers (1-5). */
public final class SearchResultItem {
    private final String code;
    private final String title;
    private final double weight;
    private final Campus campus;
    private final List<Integer> breadth;
    private final boolean offeredThisYear;

    public SearchResultItem(String code, String title, double weight,
                            Campus campus, List<Integer> breadth, boolean offeredThisYear) {
        this.code = Objects.requireNonNull(code, "code");
        this.title = Objects.requireNonNull(title, "title");
        this.weight = weight;
        this.campus = Objects.requireNonNull(campus, "campus");
        this.breadth = List.copyOf(breadth);
        this.offeredThisYear = offeredThisYear;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public double getWeight() {
        return weight;
    }

    public Campus getCampus() {
        return campus;
    }

    public List<Integer> getBreadth() {
        return breadth;
    }

    public boolean isOfferedThisYear() {
        return offeredThisYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SearchResultItem)) {
            return false;
        }
        SearchResultItem that = (SearchResultItem) o;
        return code.equals(that.code) && title.equals(that.title) && Double.compare(weight, that.weight) == 0
                && campus == that.campus && breadth.equals(that.breadth) && offeredThisYear == that.offeredThisYear;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, weight, campus, breadth, offeredThisYear);
    }

    @Override
    public String toString() {
        return "SearchResultItem{" + code + ", " + title + ", " + weight + ", " + campus + ", breadth " + breadth
                + (offeredThisYear ? "" : ", not offered") + "}";
    }
}
