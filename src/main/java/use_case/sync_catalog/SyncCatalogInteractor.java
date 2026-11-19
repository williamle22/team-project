package use_case.sync_catalog;

import entity.catalog.CatalogVersion;
import use_case.CatalogLoadException;
import use_case.CatalogReader;
import use_case.sync_catalog.SyncCatalogOutputData.Outcome;

import java.time.Instant;
import java.util.Optional;

/**
 * Use Case 1: decides whether to use the cached catalog, a new download, or neither. It loads a download before
 * installing it, so a bad download never replaces a good cache.
 */
public final class SyncCatalogInteractor implements SyncCatalogInputBoundary {
    private final CatalogSource source;
    private final CatalogCache cache;
    private final LiveCatalog live;
    private final SyncCatalogOutputBoundary presenter;

    public SyncCatalogInteractor(CatalogSource source, CatalogCache cache,
                                 LiveCatalog live, SyncCatalogOutputBoundary presenter) {
        this.source = source;
        this.cache = cache;
        this.live = live;
        this.presenter = presenter;
    }

    @Override
    public void execute() {
        presenter.prepareLoadingView();

        Optional<Instant> cachedTime = cache.hasCache() ? cache.cachedTime() : Optional.empty();
        FetchResult result;

        try {
            result = source.fetchIfNewer(cachedTime);
        } catch (CatalogUnavailableException e) {
            useCacheBecause(Outcome.OFFLINE_USING_CACHE, "Connect to the internet once to download course data.");
            return;
        }

        if (result.getKind() == FetchResult.Kind.NOT_MODIFIED) {
            CatalogReader catalog;

            try {
                catalog = cache.loadCached();
            } catch (CatalogLoadException e) {
                downloadFresh();
                return;
            }

            finish(catalog, Outcome.UP_TO_DATE);
        } else {
            installDownload(result.getDownload().get());
        }
    }

    private void downloadFresh() {
        String failMessage = "Course data is out of date and the server can't be reached.";
        FetchResult result;

        try {
            result = source.fetchIfNewer(Optional.empty());
        } catch (CatalogUnavailableException e) {
            presenter.prepareFailView(failMessage);
            return;
        }

        Optional<DownloadedCatalog> d = result.getDownload();

        if (d.isEmpty()) {
            presenter.prepareFailView(failMessage);
            return;
        }

        installDownload(d.get());
    }

    private void installDownload(DownloadedCatalog d) {
        CatalogReader catalog;

        try {
            catalog = cache.load(d);
            cache.install(d);
        } catch (CatalogLoadException e) {
            cache.discard(d);
            useCacheBecause(Outcome.UPDATE_FAILED_USING_CACHE, "The downloaded course data couldn't be read.");
            return;
        }

        finish(catalog, Outcome.UPDATED);
    }

    private void useCacheBecause(Outcome outcome, String failMessage) {
        if (!cache.hasCache()) {
            presenter.prepareFailView(failMessage);
            return;
        }

        CatalogReader catalog;

        try {
            catalog = cache.loadCached();
        } catch (CatalogLoadException e) {
            presenter.prepareFailView(failMessage);
            return;
        }

        finish(catalog, outcome);
    }

    private void finish(CatalogReader catalog, Outcome outcome) {
        live.replace(catalog);
        CatalogVersion version = catalog.version().orElseThrow();
        presenter.prepareSuccessView(new SyncCatalogOutputData(
                outcome, version.getSchoolYear(), version.getLastUpdated(),
                catalog.allCourses().size(), catalog.allPrograms().size()
        ));
    }
}
