package use_case.search_courses;

import entity.catalog.Course;

import java.util.List;

public interface SearchCoursesDataAccessInterface {
    boolean isLoaded();
    List<Course> search(String query, SearchFilters filters);
}
