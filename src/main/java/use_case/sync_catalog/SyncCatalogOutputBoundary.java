package use_case.sync_catalog;

public interface SyncCatalogOutputBoundary {
    void prepareLoadingView();
    void prepareSuccessView(SyncCatalogOutputData data);
    void prepareFailView(String message);
}
