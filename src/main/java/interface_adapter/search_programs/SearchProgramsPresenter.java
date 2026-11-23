package interface_adapter.search_programs;

import use_case.search_programs.ProgramResultItem;
import use_case.search_programs.SearchProgramsOutputBoundary;
import use_case.search_programs.SearchProgramsOutputData;

import java.util.List;
import java.util.stream.Collectors;

/** Turns the program search's results into rows and a message. */
public final class SearchProgramsPresenter implements SearchProgramsOutputBoundary {
    private static final String NO_MATCHES = "No programs match. Try fewer words or types.";

    private final SearchProgramsViewModel viewModel;

    public SearchProgramsPresenter(SearchProgramsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(SearchProgramsOutputData data) {
        List<ProgramResultRow> rows = data.getResults().stream().map(SearchProgramsPresenter::toRow)
                .collect(Collectors.toList());

        update(rows, rows.isEmpty() ? NO_MATCHES : "");
    }

    @Override
    public void prepareFailView(String message) {
        update(List.of(), message);
    }

    private void update(List<ProgramResultRow> results, String message) {
        SearchProgramsState state = new SearchProgramsState();

        state.setResults(results);
        state.setMessage(message);

        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    private static ProgramResultRow toRow(ProgramResultItem item) {
        return new ProgramResultRow(item.getCode(), item.getTitle(), item.getType().getValue());
    }
}
