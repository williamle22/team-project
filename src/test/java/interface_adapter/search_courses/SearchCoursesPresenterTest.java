package interface_adapter.search_courses;

import entity.catalog.Campus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.search_courses.SearchCoursesOutputData;
import use_case.search_courses.SearchResultItem;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchCoursesPresenterTest {
    private static final SearchResultItem CSC148 =
            new SearchResultItem("CSC148H1", "Introduction to Computer Science", 0.5, Campus.UTSG, List.of(5), true);

    private final List<PropertyChangeEvent> events = new ArrayList<>();
    private SearchCoursesViewModel viewModel;
    private SearchCoursesPresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new SearchCoursesViewModel();
        viewModel.addPropertyChangeListener(events::add);
        presenter = new SearchCoursesPresenter(viewModel);
    }

    private static SearchCoursesState state(List<SearchResultRow> results, String message, String footer) {
        SearchCoursesState s = new SearchCoursesState();
        s.setResults(results);
        s.setMessage(message);
        s.setFooter(footer);
        return s;
    }

    private void assertShown(SearchCoursesState expected) {
        assertEquals(1, events.size());
        assertEquals("state", events.get(0).getPropertyName());
        assertEquals(expected, events.get(0).getNewValue());
        assertSame(viewModel.getState(), events.get(0).getNewValue());
    }

    private SearchResultRow rowFor(SearchResultItem item) {
        presenter.prepareSuccessView(new SearchCoursesOutputData(List.of(item), 1));
        return viewModel.getState().getResults().get(0);
    }

    @Test
    void formatsRow() {
        presenter.prepareSuccessView(new SearchCoursesOutputData(List.of(CSC148), 1));
        SearchResultRow row = new SearchResultRow("CSC148H1", "Introduction to Computer Science", "0.5",
                "UTSG \u00B7 BR 5");
        assertShown(state(List.of(row), "", ""));
    }

    @Test
    void formatsFullYearCredits() {
        SearchResultItem mat137 = new SearchResultItem("MAT137Y1", "Calculus", 1.0, Campus.UTSG, List.of(5), true);
        assertEquals("1.0", rowFor(mat137).getCredits());
    }

    @Test
    void detailsJoinEveryPart() {
        SearchResultItem item = new SearchResultItem("ENV200H1", "t", 0.5, Campus.ONLINE, List.of(4, 5), false);
        assertEquals("ONLINE \u00B7 BR 4, 5 \u00B7 not offered this year", rowFor(item).getDetails());
    }

    @Test
    void detailsOmitEmptyParts() {
        SearchResultItem item = new SearchResultItem("CSC199H1", "t", 0.5, Campus.UTSG, List.of(), true);
        assertEquals("UTSG", rowFor(item).getDetails());
    }

    @Test
    void emptyResultsMessage() {
        presenter.prepareSuccessView(new SearchCoursesOutputData(List.of(), 0));
        assertShown(state(List.of(), "No courses match. Try fewer words or filters.", ""));
    }

    @Test
    void truncatedFooter() {
        List<SearchResultItem> hundred = Collections.nCopies(100, CSC148);
        presenter.prepareSuccessView(new SearchCoursesOutputData(hundred, 1412));
        assertEquals("Showing the first 100 of 1,412", viewModel.getState().getFooter());
        assertEquals(100, viewModel.getState().getResults().size());
    }

    @Test
    void failClearsResults() {
        presenter.prepareSuccessView(new SearchCoursesOutputData(List.of(CSC148), 1));
        events.clear();
        presenter.prepareFailView("Course data is still loading.");
        assertShown(state(List.of(), "Course data is still loading.", ""));
    }
}
