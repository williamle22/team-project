package interface_adapter.sync_catalog;

import use_case.sync_catalog.SyncCatalogInputBoundary;
import use_case.sync_catalog.SyncCatalogOutputBoundary;

import java.util.concurrent.CompletableFuture;

/** Starts UC1 when the app starts or when the user clicks Retry. */
public final class SyncCatalogController {
    private final SyncCatalogInputBoundary interactor;
    private final SyncCatalogOutputBoundary presenter;

    public SyncCatalogController(SyncCatalogInputBoundary interactor, SyncCatalogOutputBoundary presenter) {
        this.interactor = interactor;
        this.presenter = presenter;
    }

    /**
     * Runs the sync on a background thread and returns immediately. If the sync throws an unexpected exception, the
     * status bar shows an error.
     */
    public void execute() {
        CompletableFuture.runAsync(interactor::execute).exceptionally(e -> {
            e.printStackTrace();
            presenter.prepareFailView("Something went wrong loading course data.");
            return null;
        });
    }
}
