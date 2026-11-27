package use_case.check_graduation;

import java.util.ArrayList;
import java.util.List;

class RecordingGraduationPresenter implements CheckGraduationOutputBoundary {
    final List<String> calls = new ArrayList<>();
    CheckGraduationOutputData data;
    String failMessage;

    @Override
    public void prepareSuccessView(CheckGraduationOutputData data) {
        calls.add("success");
        this.data = data;
    }

    @Override
    public void prepareFailView(String message) {
        calls.add("fail");
        failMessage = message;
    }
}
