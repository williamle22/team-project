package interface_adapter.search_courses;

import interface_adapter.ViewModel;

public final class SearchCoursesViewModel extends ViewModel<SearchCoursesState> {
    public SearchCoursesViewModel() {
        super("search courses");
        setState(new SearchCoursesState());
    }
}
