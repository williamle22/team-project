package entity.catalog;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import testutil.CatalogFixtures;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExclusionsTest {

    @Test
    void emptyRawIsAllowed() {
        Exclusions e = new Exclusions(List.of(), List.of(), "");
        assertEquals("", e.getRaw());
        assertTrue(e.getItems().isEmpty());
    }

    @Test
    void itemsAreReadOnly() {
        Exclusions e = new Exclusions(List.of(), List.of(), "");
        assertThrows(UnsupportedOperationException.class, () -> e.getItems().add(null));
    }

    @Test
    void gettersReturnConstructorValues() {
        ExclusionItem item = CatalogFixtures.excludesCourses("CSC108H1");
        Exclusions e = new Exclusions(List.of(item), List.of("NA"), "CSC108H1");
        assertEquals(List.of(item), e.getItems());
        assertEquals(List.of("NA"), e.getNotes());
        assertEquals("CSC108H1", e.getRaw());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Exclusions.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesRaw() {
        assertTrue(new Exclusions(List.of(), List.of(), "CSC108H1").toString().contains("CSC108H1"));
    }
}
