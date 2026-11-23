package interface_adapter.search_programs;

import interface_adapter.ViewModel;

public final class SearchProgramsViewModel extends ViewModel<SearchProgramsState> {
    public SearchProgramsViewModel() {
        super("search programs");
        setState(new SearchProgramsState());
    }
}
