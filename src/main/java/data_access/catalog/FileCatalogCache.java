package data_access.catalog;

import use_case.CatalogLoadException;
import use_case.CatalogReader;
import use_case.sync_catalog.CatalogCache;
import use_case.sync_catalog.DownloadedCatalog;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;

public final class FileCatalogCache implements CatalogCache {
    private final Path dataDirectory;
    private final SqliteCatalogLoader loader;

    public FileCatalogCache(Path dataDirectory, SqliteCatalogLoader loader) {
        this.dataDirectory = dataDirectory;
        this.loader = loader;

        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException("failed to create data directory for catalog at path: " + path(), e);
        }
        System.err.println("catalog cache: " + path());
    }

    public Path path() {
        return dataDirectory.resolve("dgx.db");
    }

    @Override
    public boolean hasCache() {
        return Files.exists(path());
    }

    @Override
    public Optional<Instant> cachedTime() {
        if (!hasCache()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.getLastModifiedTime(path()).toInstant());
        } catch (IOException e) {
            throw new CatalogLoadException("could not read time of " + path(), e);
        }
    }

    @Override
    public CatalogReader loadCached() {
        return loader.load(path());
    }

    @Override
    public CatalogReader load(DownloadedCatalog d) {
        return loader.load(d.getFile());
    }

    @Override
    public void install(DownloadedCatalog d) {
        try {
            Files.move(d.getFile(), path(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            Optional<Instant> serverTime = d.getServerTime();
            if (serverTime.isPresent()) {
                Files.setLastModifiedTime(path(), FileTime.from(serverTime.get()));
            }
        } catch (IOException e) {
            throw new CatalogLoadException("could not install catalog at " + path(), e);
        }
    }

    @Override
    public void discard(DownloadedCatalog d) {
        try {
            Files.deleteIfExists(d.getFile());
        } catch (IOException ignored) {}
    }
}
