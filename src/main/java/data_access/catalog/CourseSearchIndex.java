package data_access.catalog;

import entity.catalog.Course;
import use_case.search_courses.SearchFilters;

import java.util.*;

/**
 * Finds courses by code or by title words in a fixed list of courses. It's immutable, so you can search it from any
 * thread while another thread builds a new index.
 */
public final class CourseSearchIndex {
    private final List<Entry> entries;

    public CourseSearchIndex(Collection<Course> courses) {
        List<Entry> e = new ArrayList<>();

        for (Course c : courses) {
            e.add(new Entry(c));
        }

        this.entries = e;
    }

    /** Returns an index with no courses, for use before the first catalog loads. */
    public static CourseSearchIndex empty() {
        return new CourseSearchIndex(List.of());
    }

    /**
     * Returns the courses that match {@code query} and {@code filters}, in the order the constructor received them. A
     * course matches if its code contains the query with all spaces removed, so {@code "csc 148"} finds
     * {@code CSC148H1}. It also matches if its code and title contain every word of the query. A blank query matches
     * every course.
     */
    public List<Course> search(String query, SearchFilters filters) {
        String stripped = query.strip();
        String compact = stripped.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        String[] words = stripped.toLowerCase(Locale.ROOT).split("\\s+");

        List<Course> results = new ArrayList<>();

        for (Entry e : entries) {
            if ((stripped.isBlank() || e.codeKey.contains(compact) || Arrays.stream(words).allMatch(e.text::contains))
                    && filters.accepts(e.course)) {
                results.add(e.course);
            }
        }

        return results;
    }

    private static final class Entry {
        private final Course course;
        private final String codeKey;
        private final String text;

        Entry(Course course) {
            this.course = course;
            this.codeKey = course.getCode();
            this.text = (codeKey + " " + course.getTitle()).toLowerCase(Locale.ROOT);
        }
    }
}
