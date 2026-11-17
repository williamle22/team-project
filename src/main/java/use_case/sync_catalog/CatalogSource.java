package use_case.sync_catalog;

import java.time.Instant;
import java.util.Optional;

public interface CatalogSource {
    FetchResult fetchIfNewer(Optional<Instant> cachedTime) throws CatalogUnavailableException;
}
