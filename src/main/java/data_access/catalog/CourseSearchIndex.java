package data_access.catalog;

import entity.catalog.Course;
import use_case.search_courses.SearchFilters;

import java.util.*;

public final class CourseSearchIndex {
    private final List<Entry> entries;

    public CourseSearchIndex(Collection<Course> courses) {
        List<Entry> e = new ArrayList<>();

        for (Course c : courses) {
            e.add(new Entry(c));
        }

        this.entries = e;
    }

    public static CourseSearchIndex empty() {
        return new CourseSearchIndex(List.of());
    }

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
