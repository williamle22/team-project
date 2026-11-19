package interface_adapter.search_courses;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import use_case.search_courses.SearchCoursesInputBoundary;
import use_case.search_courses.SearchCoursesInputData;
import use_case.search_courses.SearchFilters;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Starts Use Case 2 when the search text or a filter changes. The search runs on the calling thread, because it's
 * fast.
 */
public final class SearchCoursesController {
    private final SearchCoursesInputBoundary interactor;

    public SearchCoursesController(SearchCoursesInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Searches with the panel's current values. Empty sets and a blank designator mean any value. {@code campuses}
     * holds campus values as the backend writes them, such as {@code "utsg"}. {@code breadth} holds breadth category
     * numbers (1-5).
     */
    public void execute(String query, String designator, Set<Integer> levels, Set<String> campuses,
                        Set<Integer> breadth) {
        SearchFilters filters = new SearchFilters(designator, levels,
                campuses.stream().map(Campus::fromString).collect(Collectors.toSet()),
                breadth.stream().map(BreadthCategory::fromInt).collect(Collectors.toSet()));

        interactor.execute(new SearchCoursesInputData(query, filters));
    }
}
