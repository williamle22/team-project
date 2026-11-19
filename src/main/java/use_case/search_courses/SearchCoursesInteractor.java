package use_case.search_courses;

import entity.catalog.BreadthCategory;
import entity.catalog.Course;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
        matches.sort(ranking(input.getQuery()));

        int totalMatches = matches.size();
        List<Course> results = matches.subList(0, Math.min(MAX_RESULTS, matches.size()));
        List<SearchResultItem> items = results.stream().map(SearchCoursesInteractor::toItem)
                .collect(Collectors.toList());

        presenter.prepareSuccessView(new SearchCoursesOutputData(items, totalMatches));
    }

    static Comparator<Course> ranking(String query) {
        String q = query.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        return Comparator.comparingInt((Course c) -> rank(c.getCode(), q)).thenComparing(Course::getCode);
    }

    private static int rank(String code, String q) {
        if (code.equals(q)) {
            return 0;
        }

        if (code.startsWith(q)) {
            return 1;
        }

        if (code.contains(q)) {
            return 2;
        }
        return 3;
    }

    private static SearchResultItem toItem(Course course) {
        return new SearchResultItem(course.getCode(), course.getTitle(), course.getWeight(), course.getCampus(),
                course.getBreadth().stream().map(BreadthCategory::getNumber).collect(Collectors.toList()),
                course.isOfferedThisYear());
    }
}
