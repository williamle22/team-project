package interface_adapter.sync_catalog;

import interface_adapter.sync_catalog.SyncCatalogState.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.sync_catalog.SyncCatalogOutputData;
import use_case.sync_catalog.SyncCatalogOutputData.Outcome;

import java.beans.PropertyChangeEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SyncCatalogPresenterTest {
    private static final Instant NOV_5 =
            LocalDate.of(2026, 11, 5).atStartOfDay(ZoneId.systemDefault()).plusHours(12).toInstant();

    private final List<PropertyChangeEvent> events = new ArrayList<>();
    private SyncCatalogViewModel viewModel;
    private SyncCatalogPresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new SyncCatalogViewModel();
        viewModel.addPropertyChangeListener(events::add);
        presenter = new SyncCatalogPresenter(viewModel);
    }

    private static SyncCatalogState state(boolean loading, Severity severity, boolean retry, String message) {
        SyncCatalogState s = new SyncCatalogState();
        s.setLoading(loading);
        s.setSeverity(severity);
        s.setRetryAvailable(retry);
        s.setMessage(message);
        return s;
    }

    private static SyncCatalogOutputData data(Outcome outcome) {
        return new SyncCatalogOutputData(outcome, "2026-2027", NOV_5, 5368, 404);
    }

    private void assertShown(SyncCatalogState expected) {
        assertEquals(1, events.size());
        assertEquals("state", events.get(0).getPropertyName());
        assertEquals(expected, events.get(0).getNewValue());
        assertSame(viewModel.getState(), events.get(0).getNewValue());
    }

    @Test
    void loading() {
        presenter.prepareLoadingView();
        assertShown(state(true, Severity.INFO, false, "Loading course data..."));
    }

    @Test
    void updated() {
        presenter.prepareSuccessView(data(Outcome.UPDATED));
        assertShown(state(false, Severity.INFO, false, "Course data updated (2026-2027, 5,368 courses)"));
    }

    @Test
    void upToDate() {
        presenter.prepareSuccessView(data(Outcome.UP_TO_DATE));
        assertShown(state(false, Severity.INFO, false, "Course data is up to date (updated Nov 5)"));
    }

    @Test
    void offlineUsingCache() {
        presenter.prepareSuccessView(data(Outcome.OFFLINE_USING_CACHE));
        assertShown(state(false, Severity.WARNING, true, "Offline: using course data from Nov 5"));
    }

    @Test
    void updateFailedUsingCache() {
        presenter.prepareSuccessView(data(Outcome.UPDATE_FAILED_USING_CACHE));
        assertShown(state(false, Severity.WARNING, true, "Couldn't update course data; using the copy from Nov 5"));
    }

    @Test
    void fail() {
        presenter.prepareFailView("Connect to the internet once to download course data.");
        assertShown(state(false, Severity.ERROR, true, "Connect to the internet once to download course data."));
    }

    @Test
    void eachCallReplacesTheState() {
        presenter.prepareLoadingView();
        SyncCatalogState loading = viewModel.getState();
        presenter.prepareFailView("Connect to the internet once to download course data.");
        assertEquals(2, events.size());
        assertNotSame(loading, viewModel.getState());
        assertTrue(loading.isLoading());
    }
}
