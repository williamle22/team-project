package use_case.sync_catalog;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DownloadedCatalogTest {
    private static final Path FILE = Paths.get("data", "dgx.db.download");
    private static final Instant SERVER_TIME = Instant.parse("2026-11-12T10:00:04Z");

    @Test
    void gettersReturnConstructorValues() {
        DownloadedCatalog d = new DownloadedCatalog(FILE, SERVER_TIME);
        assertEquals(FILE, d.getFile());
        assertEquals(Optional.of(SERVER_TIME), d.getServerTime());
    }

    @Test
    void serverTimeMayBeMissing() {
        assertEquals(Optional.empty(), new DownloadedCatalog(FILE, null).getServerTime());
    }

    @Test
    void rejectsNullFile() {
        assertThrows(NullPointerException.class, () -> new DownloadedCatalog(null, SERVER_TIME));
    }

    @Test
    void toStringShowsFile() {
        assertTrue(new DownloadedCatalog(FILE, SERVER_TIME).toString().contains("dgx.db.download"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(DownloadedCatalog.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
