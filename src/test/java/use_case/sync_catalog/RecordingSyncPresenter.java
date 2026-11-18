package use_case.sync_catalog;

import java.util.ArrayList;
import java.util.List;

class RecordingSyncPresenter implements SyncCatalogOutputBoundary {
    final List<String> calls = new ArrayList<>();
    SyncCatalogOutputData data;
    String failMessage;

    @Override
    public void prepareLoadingView() {
        calls.add("loading");
    }

    @Override
    public void prepareSuccessView(SyncCatalogOutputData data) {
        calls.add("success");
        this.data = data;
    }

    @Override
    public void prepareFailView(String message) {
        calls.add("fail");
        failMessage = message;
    }
}
