package use_case.search_courses;

import entity.catalog.Course;

import java.util.List;

/** The catalog search that Use Case 2 needs. */
public interface SearchCoursesDataAccessInterface {
    /** Returns whether a catalog has loaded. */
    boolean isLoaded();
    /**
     * Returns every course that matches {@code query} and {@code filters}, in no particular order. A blank query
     * matches every course.
     */
    List<Course> search(String query, SearchFilters filters);
}
