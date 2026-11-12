package entity.catalog;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CatalogVersionTest {

    private static final Instant UPDATED = Instant.parse("2026-08-01T12:00:00Z");

    @Test
    void gettersReturnConstructorValues() {
        CatalogVersion v = new CatalogVersion("2026-2027", UPDATED);
        assertEquals("2026-2027", v.getSchoolYear());
        assertEquals(UPDATED, v.getLastUpdated());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(CatalogVersion.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesSchoolYear() {
        assertTrue(new CatalogVersion("2026-2027", UPDATED).toString().contains("2026-2027"));
    }
}
