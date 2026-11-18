package use_case.sync_catalog;

import java.time.Instant;
import java.util.Optional;

/** The server that provides the catalog. */
public interface CatalogSource {
    /**
     * Downloads the catalog if it changed since {@code cachedTime}. If {@code cachedTime} is empty, it always
     * downloads.
     *
     * @throws CatalogUnavailableException if the app can't reach the server or the server returns an error
     */
    FetchResult fetchIfNewer(Optional<Instant> cachedTime) throws CatalogUnavailableException;
}
