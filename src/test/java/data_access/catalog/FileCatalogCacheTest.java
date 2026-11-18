package data_access.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import use_case.CatalogLoadException;
import use_case.sync_catalog.DownloadedCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FileCatalogCacheTest {
    private static final Instant SERVER_TIME = Instant.parse("2026-11-12T10:00:04Z");

    @TempDir
    Path dir;

    private final List<Path> loaded = new ArrayList<>();
    private final InMemoryCatalogRepository catalog = InMemoryCatalogRepository.empty();
    private FileCatalogCache cache;

    @BeforeEach
    void setUp() {
        SqliteCatalogLoader loader = new SqliteCatalogLoader() {
            @Override
            public InMemoryCatalogRepository load(Path dbFile) {
                loaded.add(dbFile);
                return catalog;
            }
        };
        cache = new FileCatalogCache(dir, loader);
    }

    private Path download(String content) throws IOException {
        return Files.writeString(dir.resolve("dgx-1.db.part"), content);
    }

    @Test
    void createsDataDirectory() {
        Path nested = dir.resolve("a").resolve("b");
        new FileCatalogCache(nested, new SqliteCatalogLoader());
        assertTrue(Files.isDirectory(nested));
    }

    @Test
    void pathIsDgxDbInDataDirectory() {
        assertEquals(dir.resolve("dgx.db"), cache.path());
    }

    @Test
    void noCacheInitially() {
        assertFalse(cache.hasCache());
        assertEquals(Optional.empty(), cache.cachedTime());
    }

    @Test
    void cachedTimeReadsFileTime() throws IOException {
        Files.writeString(cache.path(), "catalog");
        Files.setLastModifiedTime(cache.path(), FileTime.from(SERVER_TIME));
        assertTrue(cache.hasCache());
        assertEquals(Optional.of(SERVER_TIME), cache.cachedTime());
    }

    @Test
    void installMovesAndSetsTime() throws IOException {
        Path part = download("new catalog");
        cache.install(new DownloadedCatalog(part, SERVER_TIME));
        assertFalse(Files.exists(part));
        assertEquals("new catalog", Files.readString(cache.path()));
        assertEquals(Optional.of(SERVER_TIME), cache.cachedTime());
    }

    @Test
    void installReplacesExistingCache() throws IOException {
        Files.writeString(cache.path(), "old catalog");
        cache.install(new DownloadedCatalog(download("new catalog"), SERVER_TIME));
        assertEquals("new catalog", Files.readString(cache.path()));
    }

    @Test
    void installWithoutServerTimeKeepsFile() throws IOException {
        cache.install(new DownloadedCatalog(download("new catalog"), null));
        assertTrue(cache.hasCache());
        assertEquals("new catalog", Files.readString(cache.path()));
    }

    @Test
    void installMissingFileThrows() {
        DownloadedCatalog missing = new DownloadedCatalog(dir.resolve("gone.db.part"), SERVER_TIME);
        assertThrows(CatalogLoadException.class, () -> cache.install(missing));
        assertFalse(cache.hasCache());
    }

    @Test
    void discardDeletes() throws IOException {
        Path part = download("bad catalog");
        cache.discard(new DownloadedCatalog(part, SERVER_TIME));
        assertFalse(Files.exists(part));
    }

    @Test
    void discardMissingFileIsFine() {
        assertDoesNotThrow(() -> cache.discard(new DownloadedCatalog(dir.resolve("gone.db.part"), null)));
    }

    @Test
    void loadCachedReadsCachePath() {
        assertSame(catalog, cache.loadCached());
        assertEquals(List.of(cache.path()), loaded);
    }

    @Test
    void loadReadsDownloadedFile() throws IOException {
        Path part = download("new catalog");
        assertSame(catalog, cache.load(new DownloadedCatalog(part, SERVER_TIME)));
        assertEquals(List.of(part), loaded);
    }
}
