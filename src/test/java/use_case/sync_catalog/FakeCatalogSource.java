package use_case.sync_catalog;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

class FakeCatalogSource implements CatalogSource {
    private final Deque<Object> responses = new ArrayDeque<>();
    final List<Optional<Instant>> requests = new ArrayList<>();

    FakeCatalogSource thenReturn(FetchResult result) {
        responses.add(result);
        return this;
    }

    FakeCatalogSource thenOffline() {
        responses.add(new CatalogUnavailableException("network error"));
        return this;
    }

    @Override
    public FetchResult fetchIfNewer(Optional<Instant> cachedTime) throws CatalogUnavailableException {
        requests.add(cachedTime);
        Object response = responses.remove();
        if (response instanceof CatalogUnavailableException) {
            throw (CatalogUnavailableException) response;
        }
        return (FetchResult) response;
    }
}
