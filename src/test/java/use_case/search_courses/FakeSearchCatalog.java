package use_case.search_courses;

import entity.catalog.Course;

import java.util.List;

class FakeSearchCatalog implements SearchCoursesDataAccessInterface {
    boolean loaded = true;
    List<Course> matches = List.of();
    int searches;
    String lastQuery;
    SearchFilters lastFilters;

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public List<Course> search(String query, SearchFilters filters) {
        searches++;
        lastQuery = query;
        lastFilters = filters;
        return List.copyOf(matches);
    }
}
