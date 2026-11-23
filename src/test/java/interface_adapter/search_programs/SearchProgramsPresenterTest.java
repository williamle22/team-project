package interface_adapter.search_programs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.search_programs.ProgramResultItem;
import use_case.search_programs.SearchProgramsOutputData;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static org.junit.jupiter.api.Assertions.*;

class SearchProgramsPresenterTest {
    private static final ProgramResultItem CS_MAJOR =
            new ProgramResultItem("ASMAJ1689", "Computer Science Major (Science Program)", MAJOR);
    private static final ProgramResultItem CS_MINOR =
            new ProgramResultItem("ASMIN1689", "Computer Science Minor (Science Program)", MINOR);

    private final List<PropertyChangeEvent> events = new ArrayList<>();
    private SearchProgramsViewModel viewModel;
    private SearchProgramsPresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new SearchProgramsViewModel();
        viewModel.addPropertyChangeListener(events::add);
        presenter = new SearchProgramsPresenter(viewModel);
    }

    private static SearchProgramsState state(List<ProgramResultRow> results, String message) {
        SearchProgramsState s = new SearchProgramsState();
        s.setResults(results);
        s.setMessage(message);
        return s;
    }

    private void assertShown(SearchProgramsState expected) {
        assertEquals(1, events.size());
        assertEquals("state", events.get(0).getPropertyName());
        assertEquals(expected, events.get(0).getNewValue());
        assertSame(viewModel.getState(), events.get(0).getNewValue());
    }

    @Test
    void formatsRows() {
        presenter.prepareSuccessView(new SearchProgramsOutputData(List.of(CS_MAJOR, CS_MINOR)));
        assertShown(state(List.of(
                new ProgramResultRow("ASMAJ1689", "Computer Science Major (Science Program)", "Major"),
                new ProgramResultRow("ASMIN1689", "Computer Science Minor (Science Program)", "Minor")), ""));
    }

    @Test
    void emptyResultsMessage() {
        presenter.prepareSuccessView(new SearchProgramsOutputData(List.of()));
        assertShown(state(List.of(), "No programs match. Try fewer words or types."));
    }

    @Test
    void resultsClearMessage() {
        presenter.prepareSuccessView(new SearchProgramsOutputData(List.of()));
        events.clear();
        presenter.prepareSuccessView(new SearchProgramsOutputData(List.of(CS_MAJOR)));
        assertEquals("", viewModel.getState().getMessage());
        assertEquals(1, events.size());
    }

    @Test
    void failClearsResults() {
        presenter.prepareSuccessView(new SearchProgramsOutputData(List.of(CS_MAJOR)));
        events.clear();
        presenter.prepareFailView("Program data is still loading.");
        assertShown(state(List.of(), "Program data is still loading."));
    }
}
