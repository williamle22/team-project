package interface_adapter.dgx;

import interface_adapter.ViewModel;

public class CourseSearchViewModel extends ViewModel<CourseSearchState> {
    public CourseSearchViewModel() {
        super("course search panel");
        setState(new CourseSearchState());
    }
}
