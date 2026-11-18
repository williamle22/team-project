package use_case.sync_catalog;

/**
 * Receives Use Case 1's results. The interactor calls {@link #prepareLoadingView()} first, then exactly one of the
 * other two methods.
 */
public interface SyncCatalogOutputBoundary {
    void prepareLoadingView();
    void prepareSuccessView(SyncCatalogOutputData data);
    /** The interactor calls this when no catalog is available. Show {@code message} to the user. */
    void prepareFailView(String message);
}
