package use_case.search_courses;

import entity.catalog.BreadthCategory;
import entity.catalog.Course;
import use_case.CodeRanking;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case 2: ranks the matching courses and keeps the first 100. An exact code match comes first, then codes that
 * start with the query, then codes that contain it, then title matches. Courses with the same rank sort by code.
 */
public final class SearchCoursesInteractor implements SearchCoursesInputBoundary {
    static final int MAX_RESULTS = 100;

    private final SearchCoursesDataAccessInterface catalog;
    private final SearchCoursesOutputBoundary presenter;

    public SearchCoursesInteractor(SearchCoursesDataAccessInterface catalog, SearchCoursesOutputBoundary presenter) {
        this.catalog = catalog;
        this.presenter = presenter;
    }

    @Override
    public void execute(SearchCoursesInputData input) {
        if (!catalog.isLoaded()) {
            presenter.prepareFailView("Course data is still loading.");
            return;
        }

        List<Course> matches = new ArrayList<>(catalog.search(input.getQuery(), input.getFilters()));
        matches.sort(CodeRanking.by(input.getQuery(), Course::getCode));

        int totalMatches = matches.size();
        List<Course> results = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        List<SearchResultItem> items = results.stream().map(SearchCoursesInteractor::toItem)
                .collect(Collectors.toList());

        presenter.prepareSuccessView(new SearchCoursesOutputData(items, totalMatches));
    }

    private static SearchResultItem toItem(Course course) {
        return new SearchResultItem(course.getCode(), course.getTitle(), course.getWeight(), course.getCampus(),
                course.getBreadth().stream().map(BreadthCategory::getNumber).collect(Collectors.toList()),
                course.isOfferedThisYear());
    }
}
