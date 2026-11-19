package use_case.sync_catalog;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FetchResultTest {
    private static final DownloadedCatalog DOWNLOAD =
            new DownloadedCatalog(Paths.get("data", "dgx.db.download"), Instant.parse("2026-11-12T10:00:04Z"));

    @Test
    void notModifiedHasNoDownload() {
        FetchResult r = FetchResult.notModified();
        assertEquals(FetchResult.Kind.NOT_MODIFIED, r.getKind());
        assertEquals(Optional.empty(), r.getDownload());
    }

    @Test
    void downloadedKeepsDownload() {
        FetchResult r = FetchResult.downloaded(DOWNLOAD);
        assertEquals(FetchResult.Kind.DOWNLOADED, r.getKind());
        assertEquals(Optional.of(DOWNLOAD), r.getDownload());
    }

    @Test
    void downloadedRejectsNull() {
        assertThrows(NullPointerException.class, () -> FetchResult.downloaded(null));
    }

    @Test
    void toStringShowsKind() {
        assertTrue(FetchResult.notModified().toString().contains("NOT_MODIFIED"));
        assertTrue(FetchResult.downloaded(DOWNLOAD).toString().contains("DOWNLOADED"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(FetchResult.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
