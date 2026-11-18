package use_case.sync_catalog;

import use_case.CatalogReader;

/** The catalog that every use case reads. UC1 replaces it when a catalog loads. */
public interface LiveCatalog {
    void replace(CatalogReader catalog);
}
