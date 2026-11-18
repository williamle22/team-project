package use_case.sync_catalog;

import data_access.catalog.InMemoryCatalogRepository;
import entity.catalog.CatalogVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.CatalogLoadException;
import use_case.CatalogOutdatedException;
import use_case.CatalogReader;
import use_case.sync_catalog.SyncCatalogOutputData.Outcome;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.course;

class SyncCatalogInteractorTest {
    private static final Instant CACHED_TIME = Instant.parse("2025-10-17T09:00:00Z");
    private static final Instant SERVER_TIME = Instant.parse("2026-11-12T10:00:04Z");
    private static final CatalogReader OLD = new InMemoryCatalogRepository(
            List.of(course("CSC148H1")), List.of(), new CatalogVersion("2025-2026", CACHED_TIME));
    private static final CatalogReader NEW = new InMemoryCatalogRepository(
            List.of(course("CSC148H1"), course("CSC207H1")), List.of(), new CatalogVersion("2026-2027", SERVER_TIME));
    private static final DownloadedCatalog DOWNLOAD =
            new DownloadedCatalog(Paths.get("data", "dgx-1.db.part"), SERVER_TIME);
    private static final String CONNECT = "Connect to the internet once to download course data.";
    private static final String UNREADABLE = "The downloaded course data couldn't be read.";
    private static final String OUT_OF_DATE = "Course data is out of date and the server can't be reached.";

    private FakeCatalogSource source;
    private FakeCatalogCache cache;
    private RecordingLiveCatalog live;
    private RecordingSyncPresenter presenter;

    @BeforeEach
    void setUp() {
        source = new FakeCatalogSource();
        cache = new FakeCatalogCache();
        live = new RecordingLiveCatalog();
        presenter = new RecordingSyncPresenter();
    }

    private void withGoodCache() {
        cache.hasCache = true;
        cache.cachedTime = CACHED_TIME;
        cache.cached = OLD;
    }

    private void run() {
        new SyncCatalogInteractor(source, cache, live, presenter).execute();
    }

    private void assertSuccess(Outcome outcome, CatalogReader catalog) {
        assertEquals(List.of("loading", "success"), presenter.calls);
        assertEquals(outcome, presenter.data.getOutcome());
        assertSame(catalog, live.current);
    }

    private void assertFail(String message) {
        assertEquals(List.of("loading", "fail"), presenter.calls);
        assertEquals(message, presenter.failMessage);
        assertNull(live.current);
    }

    @Test
    void loadingShownFirst() {
        withGoodCache();
        source.thenReturn(FetchResult.notModified());
        run();
        assertEquals("loading", presenter.calls.get(0));
    }

    @Test
    void notModifiedUsesCache() {
        withGoodCache();
        source.thenReturn(FetchResult.notModified());
        run();
        assertSuccess(Outcome.UP_TO_DATE, OLD);
        assertEquals(List.of(Optional.of(CACHED_TIME)), source.requests);
        assertTrue(cache.installed.isEmpty());
    }

    @Test
    void successReportsVersionAndCounts() {
        withGoodCache();
        source.thenReturn(FetchResult.downloaded(DOWNLOAD));
        cache.downloaded = NEW;
        run();
        assertEquals(new SyncCatalogOutputData(Outcome.UPDATED, "2026-2027", SERVER_TIME, 2, 0), presenter.data);
    }

    @Test
    void downloadedIsInstalled() {
        withGoodCache();
        source.thenReturn(FetchResult.downloaded(DOWNLOAD));
        cache.downloaded = NEW;
        run();
        assertSuccess(Outcome.UPDATED, NEW);
        assertEquals(List.of(DOWNLOAD), cache.installed);
        assertTrue(cache.discarded.isEmpty());
    }

    @Test
    void noCacheSendsNoTime() {
        source.thenReturn(FetchResult.downloaded(DOWNLOAD));
        cache.downloaded = NEW;
        run();
        assertSuccess(Outcome.UPDATED, NEW);
        assertEquals(List.of(Optional.empty()), source.requests);
    }

    @Test
    void outdatedCacheRefetches() {
        withGoodCache();
        cache.cachedError = new CatalogOutdatedException("schema 6 is older than 7");
        source.thenReturn(FetchResult.notModified()).thenReturn(FetchResult.downloaded(DOWNLOAD));
        cache.downloaded = NEW;
        run();
        assertSuccess(Outcome.UPDATED, NEW);
        assertEquals(List.of(Optional.of(CACHED_TIME), Optional.empty()), source.requests);
        assertEquals(List.of(DOWNLOAD), cache.installed);
    }

    @Test
    void corruptCacheRefetches() {
        withGoodCache();
        cache.cachedError = new CatalogLoadException("file is not a database");
        source.thenReturn(FetchResult.notModified()).thenReturn(FetchResult.downloaded(DOWNLOAD));
        cache.downloaded = NEW;
        run();
        assertSuccess(Outcome.UPDATED, NEW);
    }

    @Test
    void badDownloadKeepsCache() {
        withGoodCache();
        source.thenReturn(FetchResult.downloaded(DOWNLOAD));
        run();
        assertSuccess(Outcome.UPDATE_FAILED_USING_CACHE, OLD);
        assertEquals(List.of(DOWNLOAD), cache.discarded);
        assertTrue(cache.installed.isEmpty());
    }

    @Test
    void badDownloadNoCacheFails() {
        source.thenReturn(FetchResult.downloaded(DOWNLOAD));
        run();
        assertFail(UNREADABLE);
        assertEquals(List.of(DOWNLOAD), cache.discarded);
    }

    @Test
    void installFailureKeepsCache() {
        withGoodCache();
        source.thenReturn(FetchResult.downloaded(DOWNLOAD));
        cache.downloaded = NEW;
        cache.installFails = true;
        run();
        assertSuccess(Outcome.UPDATE_FAILED_USING_CACHE, OLD);
        assertEquals(List.of(DOWNLOAD), cache.discarded);
    }

    @Test
    void offlineWithCache() {
        withGoodCache();
        source.thenOffline();
        run();
        assertSuccess(Outcome.OFFLINE_USING_CACHE, OLD);
    }

    @Test
    void offlineNoCacheFails() {
        source.thenOffline();
        run();
        assertFail(CONNECT);
    }

    @Test
    void offlineBrokenCacheFails() {
        withGoodCache();
        cache.cachedError = new CatalogLoadException("file is not a database");
        source.thenOffline();
        run();
        assertFail(CONNECT);
    }

    @Test
    void outdatedCacheAndOfflineFails() {
        withGoodCache();
        cache.cachedError = new CatalogOutdatedException("schema 6 is older than 7");
        source.thenReturn(FetchResult.notModified()).thenOffline();
        run();
        assertFail(OUT_OF_DATE);
    }

    @Test
    void secondFetchNotModifiedFails() {
        withGoodCache();
        cache.cachedError = new CatalogOutdatedException("schema 6 is older than 7");
        source.thenReturn(FetchResult.notModified()).thenReturn(FetchResult.notModified());
        run();
        assertFail(OUT_OF_DATE);
        assertEquals(2, source.requests.size());
    }
}
