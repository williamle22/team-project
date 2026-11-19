package interface_adapter.search_courses;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import use_case.search_courses.SearchCoursesInputBoundary;
import use_case.search_courses.SearchCoursesInputData;
import use_case.search_courses.SearchFilters;

import java.util.Set;
import java.util.stream.Collectors;

public final class SearchCoursesController {
    private final SearchCoursesInputBoundary interactor;

    public SearchCoursesController(SearchCoursesInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(String query, String designator, Set<Integer> levels, Set<String> campuses,
                        Set<Integer> breadth) {
        SearchFilters filters = new SearchFilters(designator, levels,
                campuses.stream().map(Campus::fromString).collect(Collectors.toSet()),
                breadth.stream().map(BreadthCategory::fromInt).collect(Collectors.toSet()));

        interactor.execute(new SearchCoursesInputData(query, filters));
    }
}
