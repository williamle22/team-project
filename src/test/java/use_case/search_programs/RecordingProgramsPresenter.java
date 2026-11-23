package use_case.search_programs;

import java.util.ArrayList;
import java.util.List;

class RecordingProgramsPresenter implements SearchProgramsOutputBoundary {
    final List<String> calls = new ArrayList<>();
    SearchProgramsOutputData data;
    String failMessage;

    @Override
    public void prepareSuccessView(SearchProgramsOutputData data) {
        calls.add("success");
        this.data = data;
    }

    @Override
    public void prepareFailView(String message) {
        calls.add("fail");
        failMessage = message;
    }
}
