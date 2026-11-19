package interface_adapter.search_courses;

import interface_adapter.ViewModel;

/** The view model for the search panel. */
public final class SearchCoursesViewModel extends ViewModel<SearchCoursesState> {
    public SearchCoursesViewModel() {
        super("search courses");
        setState(new SearchCoursesState());
    }
}
