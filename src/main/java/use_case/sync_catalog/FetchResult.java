package use_case.sync_catalog;

import java.util.Objects;
import java.util.Optional;

public final class FetchResult {
    public enum Kind { NOT_MODIFIED, DOWNLOADED }

    private final Kind kind;
    private final DownloadedCatalog download;

    private FetchResult(Kind kind, DownloadedCatalog download) {
        this.kind = kind;
        this.download = download;
    }

    public static FetchResult notModified() {
        return new FetchResult(Kind.NOT_MODIFIED, null);
    }

    public static FetchResult downloaded(DownloadedCatalog download) {
        return new FetchResult(Kind.DOWNLOADED, Objects.requireNonNull(download, "download"));
    }

    public Kind getKind() {
        return kind;
    }

    public Optional<DownloadedCatalog> getDownload() {
        return Optional.ofNullable(download);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FetchResult)) {
            return false;
        }
        FetchResult that = (FetchResult) o;
        return kind == that.kind && Objects.equals(download, that.download);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, download);
    }

    @Override
    public String toString() {
        return "FetchResult{" + kind + ", " + download + "}";
    }
}
