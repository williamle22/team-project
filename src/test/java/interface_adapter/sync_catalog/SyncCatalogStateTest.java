package interface_adapter.sync_catalog;

import interface_adapter.sync_catalog.SyncCatalogState.Severity;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SyncCatalogStateTest {

    @Test
    void defaultsToBlankInfo() {
        SyncCatalogState s = new SyncCatalogState();
        assertFalse(s.isLoading());
        assertEquals("", s.getMessage());
        assertEquals(Severity.INFO, s.getSeverity());
        assertFalse(s.isRetryAvailable());
    }

    @Test
    void settersUpdateFields() {
        SyncCatalogState s = new SyncCatalogState();
        s.setLoading(true);
        s.setMessage("Offline: using course data from Nov 5");
        s.setSeverity(Severity.WARNING);
        s.setRetryAvailable(true);
        assertTrue(s.isLoading());
        assertEquals("Offline: using course data from Nov 5", s.getMessage());
        assertEquals(Severity.WARNING, s.getSeverity());
        assertTrue(s.isRetryAvailable());
    }

    @Test
    void toStringShowsSeverityMessageAndFlags() {
        SyncCatalogState s = new SyncCatalogState();
        s.setMessage("Loading course data...");
        assertEquals("SyncCatalogState{INFO, Loading course data...}", s.toString());
        s.setLoading(true);
        s.setRetryAvailable(true);
        assertEquals("SyncCatalogState{INFO, Loading course data..., loading, retry}", s.toString());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SyncCatalogState.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
