package use_case.sync_catalog;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import use_case.sync_catalog.SyncCatalogOutputData.Outcome;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SyncCatalogOutputDataTest {
    private static final Instant UPDATED = Instant.parse("2026-11-12T10:00:04Z");

    @Test
    void gettersReturnConstructorValues() {
        SyncCatalogOutputData d = new SyncCatalogOutputData(Outcome.UPDATED, "2026-2027", UPDATED, 5368, 404);
        assertEquals(Outcome.UPDATED, d.getOutcome());
        assertEquals("2026-2027", d.getSchoolYear());
        assertEquals(UPDATED, d.getLastUpdated());
        assertEquals(5368, d.getCourseCount());
        assertEquals(404, d.getProgramCount());
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new SyncCatalogOutputData(null, "2026-2027", UPDATED, 0, 0));
        assertThrows(NullPointerException.class, () -> new SyncCatalogOutputData(Outcome.UPDATED, null, UPDATED, 0, 0));
        assertThrows(NullPointerException.class, () -> new SyncCatalogOutputData(Outcome.UPDATED, "2026-2027", null, 0, 0));
    }

    @Test
    void toStringShowsOutcomeAndCounts() {
        String s = new SyncCatalogOutputData(Outcome.UP_TO_DATE, "2026-2027", UPDATED, 5368, 404).toString();
        assertTrue(s.contains("UP_TO_DATE"));
        assertTrue(s.contains("5368"));
        assertTrue(s.contains("404"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SyncCatalogOutputData.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
