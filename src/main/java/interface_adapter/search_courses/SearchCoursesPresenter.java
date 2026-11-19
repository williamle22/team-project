package interface_adapter.search_courses;

import use_case.search_courses.SearchCoursesOutputBoundary;
import use_case.search_courses.SearchCoursesOutputData;
import use_case.search_courses.SearchResultItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Turns Use Case 2's results into list rows, a message, and a footer. */
public final class SearchCoursesPresenter implements SearchCoursesOutputBoundary {
    private static final String NO_MATCHES = "No courses match. Try fewer words or filters.";
    private static final String SEPARATOR = " \u00B7 ";

    private final SearchCoursesViewModel viewModel;

    public SearchCoursesPresenter(SearchCoursesViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(SearchCoursesOutputData data) {
        List<SearchResultRow> rows = data.getResults().stream().map(SearchCoursesPresenter::toRow)
                .collect(Collectors.toList());

        String message = rows.isEmpty() ? NO_MATCHES : "";

        String footer = "";
        if (data.isTruncated()) {
            footer = String.format(Locale.ENGLISH, "Showing the first %d of %,d", rows.size(), data.getTotalMatches());
        }

        update(rows, message, footer);
    }

    @Override
    public void prepareFailView(String message) {
        update(List.of(), message, "");
    }

    private void update(List<SearchResultRow> results, String message, String footer) {
        SearchCoursesState state = new SearchCoursesState();

        state.setResults(results);
        state.setMessage(message);
        state.setFooter(footer);

        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    private static SearchResultRow toRow(SearchResultItem item) {
        List<String> details = new ArrayList<>();
        details.add(item.getCampus().name());
        if (!item.getBreadth().isEmpty()) {
            details.add("BR " + item.getBreadth().stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }
        if (!item.isOfferedThisYear()) {
            details.add("not offered this year");
        }

        return new SearchResultRow(item.getCode(), item.getTitle(),
                String.format(Locale.ROOT, "%.1f", item.getWeight()), String.join(SEPARATOR, details));
    }
}
