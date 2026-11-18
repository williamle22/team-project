package use_case.sync_catalog;

import use_case.CatalogReader;

class RecordingLiveCatalog implements LiveCatalog {
    CatalogReader current;

    @Override
    public void replace(CatalogReader catalog) {
        current = catalog;
    }
}
