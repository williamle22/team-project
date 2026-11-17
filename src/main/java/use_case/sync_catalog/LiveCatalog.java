package use_case.sync_catalog;

import use_case.CatalogReader;

public interface LiveCatalog {
    void replace(CatalogReader catalog);
}
