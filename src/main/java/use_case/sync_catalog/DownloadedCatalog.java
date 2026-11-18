package use_case.sync_catalog;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * A downloaded catalog file that isn't installed yet, with the server's {@code Last-Modified} time if the server sent
 * one.
 */
public final class DownloadedCatalog {
    private final Path file;
    private final Instant serverTime;

    public DownloadedCatalog(Path file, Instant serverTime) {
        this.file = Objects.requireNonNull(file, "file");
        this.serverTime = serverTime;
    }

    public Path getFile() {
        return file;
    }

    public Optional<Instant> getServerTime() {
        return Optional.ofNullable(serverTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DownloadedCatalog)) {
            return false;
        }
        DownloadedCatalog that = (DownloadedCatalog) o;
        return file.equals(that.file) && Objects.equals(serverTime, that.serverTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, serverTime);
    }

    @Override
    public String toString() {
        return "DownloadedCatalog{" + file + ", " + serverTime + "}";
    }
}
