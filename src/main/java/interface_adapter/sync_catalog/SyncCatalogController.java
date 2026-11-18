package interface_adapter.sync_catalog;

import use_case.sync_catalog.SyncCatalogInputBoundary;
import use_case.sync_catalog.SyncCatalogOutputBoundary;

import java.util.concurrent.CompletableFuture;

public final class SyncCatalogController {
    private final SyncCatalogInputBoundary interactor;
    private final SyncCatalogOutputBoundary presenter;

    public SyncCatalogController(SyncCatalogInputBoundary interactor, SyncCatalogOutputBoundary presenter) {
        this.interactor = interactor;
        this.presenter = presenter;
    }

    public void execute() {
        CompletableFuture.runAsync(interactor::execute).exceptionally(e -> {
            e.printStackTrace();
            presenter.prepareFailView("Something went wrong loading course data.");
            return null;
        });
    }
}
