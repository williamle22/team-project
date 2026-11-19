package use_case.search_courses;

import java.util.ArrayList;
import java.util.List;

class RecordingSearchPresenter implements SearchCoursesOutputBoundary {
    final List<String> calls = new ArrayList<>();
    SearchCoursesOutputData data;
    String failMessage;

    @Override
    public void prepareSuccessView(SearchCoursesOutputData data) {
        calls.add("success");
        this.data = data;
    }

    @Override
    public void prepareFailView(String message) {
        calls.add("fail");
        failMessage = message;
    }
}
