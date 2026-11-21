package use_case.check_eligibility;

import java.util.ArrayList;
import java.util.List;

class RecordingEligibilityPresenter implements CheckEligibilityOutputBoundary {
    final List<String> calls = new ArrayList<>();
    CheckEligibilityOutputData data;
    String failMessage;

    @Override
    public void prepareSuccessView(CheckEligibilityOutputData data) {
        calls.add("success");
        this.data = data;
    }

    @Override
    public void prepareFailView(String message) {
        calls.add("fail");
        failMessage = message;
    }
}
