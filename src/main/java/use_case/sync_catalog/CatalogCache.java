package use_case.sync_catalog;

import use_case.CatalogReader;

import java.time.Instant;
import java.util.Optional;

public interface CatalogCache {
    boolean hasCache();
    Optional<Instant> cachedTime();
    CatalogReader loadCached();
    CatalogReader load(DownloadedCatalog d);
    void install(DownloadedCatalog d);
    void discard(DownloadedCatalog d);
}
