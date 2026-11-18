package interface_adapter.sync_catalog;

import use_case.sync_catalog.SyncCatalogOutputBoundary;
import interface_adapter.sync_catalog.SyncCatalogState.Severity;
import use_case.sync_catalog.SyncCatalogOutputData;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SyncCatalogPresenter implements SyncCatalogOutputBoundary {
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    private final SyncCatalogViewModel viewModel;

    public SyncCatalogPresenter(SyncCatalogViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareLoadingView() {
        update(true, Severity.INFO, false, "Loading course data...");
    }

    @Override
    public void prepareSuccessView(SyncCatalogOutputData data) {
        String date = DATE.format(data.getLastUpdated());

        switch (data.getOutcome()) {
            case UPDATED:
                update(false, Severity.INFO, false,
                        "Course data updated (" + data.getSchoolYear() + ", " + String.format(Locale.ENGLISH, "%,d", data.getCourseCount()) + " courses)");
                break;
            case UP_TO_DATE:
                update(false, Severity.INFO, false,
                        "Course data is up to date (updated " + date + ")");
                break;
            case OFFLINE_USING_CACHE:
                update(false, Severity.WARNING, true,
                        "Offline: using course data from " + date);
                break;
            case UPDATE_FAILED_USING_CACHE:
                update(false, Severity.WARNING, true,
                        "Couldn't update course data; using the copy from " + date);
                break;
        }
    }

    @Override
    public void prepareFailView(String message) {
        update(false, Severity.ERROR, true, message);
    }

    private void update(boolean loading, Severity severity, boolean retryAvailable, String message) {
        SyncCatalogState state = new SyncCatalogState();

        state.setLoading(loading);
        state.setSeverity(severity);
        state.setRetryAvailable(retryAvailable);
        state.setMessage(message);

        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }
}
