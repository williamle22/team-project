package use_case;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlanSummaryTest {
    private static final UUID ID = UUID.fromString("3f2b8c1e-7a4d-4e8b-9c0a-1d2e3f4a5b6c");
    private static final Instant MODIFIED = Instant.parse("2026-09-08T14:30:00Z");

    @Test
    void gettersReturnConstructorValues() {
        PlanSummary s = new PlanSummary(ID, "CS specialist plan", MODIFIED);
        assertEquals(ID, s.getId());
        assertEquals("CS specialist plan", s.getName());
        assertEquals(MODIFIED, s.getLastModified());
    }

    @Test
    void toStringShowsName() {
        assertTrue(new PlanSummary(ID, "CS specialist plan", MODIFIED).toString().contains("CS specialist plan"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(PlanSummary.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
