package use_case.sync_catalog;

import use_case.CatalogLoadException;
import use_case.CatalogReader;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class FakeCatalogCache implements CatalogCache {
    boolean hasCache;
    Instant cachedTime;
    CatalogReader cached;
    CatalogLoadException cachedError;
    CatalogReader downloaded;
    boolean installFails;
    final List<DownloadedCatalog> installed = new ArrayList<>();
    final List<DownloadedCatalog> discarded = new ArrayList<>();

    @Override
    public boolean hasCache() {
        return hasCache;
    }

    @Override
    public Optional<Instant> cachedTime() {
        return Optional.of(cachedTime);
    }

    @Override
    public CatalogReader loadCached() {
        if (cachedError != null) {
            throw cachedError;
        }
        return cached;
    }

    @Override
    public CatalogReader load(DownloadedCatalog d) {
        if (downloaded == null) {
            throw new CatalogLoadException("bad row for course CSC148H1");
        }
        return downloaded;
    }

    @Override
    public void install(DownloadedCatalog d) {
        if (installFails) {
            throw new CatalogLoadException("could not move download");
        }
        installed.add(d);
    }

    @Override
    public void discard(DownloadedCatalog d) {
        discarded.add(d);
    }
}
