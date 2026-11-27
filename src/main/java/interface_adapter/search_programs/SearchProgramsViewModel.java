package interface_adapter.search_programs;

import interface_adapter.ViewModel;

/** The view model for the program picker. */
public final class SearchProgramsViewModel extends ViewModel<SearchProgramsState> {
    public SearchProgramsViewModel() {
        super("search programs");
        setState(new SearchProgramsState());
    }
}
