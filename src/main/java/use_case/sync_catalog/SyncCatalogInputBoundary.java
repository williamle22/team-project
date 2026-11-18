package use_case.sync_catalog;

/** Use Case 1: Sync Catalog. */
public interface SyncCatalogInputBoundary {
    /** Brings the catalog up to date and loads it, falling back to the cache when needed. */
    void execute();
}
