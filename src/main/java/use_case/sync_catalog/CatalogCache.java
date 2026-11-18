package use_case.sync_catalog;

import use_case.CatalogReader;

import java.time.Instant;
import java.util.Optional;

/**
 * The local copy of the catalog. The load methods throw {@link use_case.CatalogLoadException CatalogLoadException}, or
 * {@link use_case.CatalogOutdatedException CatalogOutdatedException} if the file uses an older schema than the app
 * supports.
 */
public interface CatalogCache {
    boolean hasCache();
    /** Returns the cached copy's server time, or an empty {@code Optional} if there's no cache. */
    Optional<Instant> cachedTime();
    CatalogReader loadCached();
    /** Loads a download without installing it, so a bad download can't replace a good cache. */
    CatalogReader load(DownloadedCatalog d);
    /**
     * Replaces the cache with the download.
     *
     * @throws use_case.CatalogLoadException if the file can't be moved
     */
    void install(DownloadedCatalog d);
    /** Deletes the download. It never throws. */
    void discard(DownloadedCatalog d);
}
