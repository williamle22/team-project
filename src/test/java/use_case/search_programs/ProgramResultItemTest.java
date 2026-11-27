package use_case.search_programs;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static entity.catalog.ProgramType.MAJOR;
import static org.junit.jupiter.api.Assertions.*;

class ProgramResultItemTest {
    private static final ProgramResultItem CS_MAJOR =
            new ProgramResultItem("ASMAJ1689", "Computer Science Major (Science Program)", MAJOR);

    @Test
    void gettersReturnConstructorValues() {
        assertEquals("ASMAJ1689", CS_MAJOR.getCode());
        assertEquals("Computer Science Major (Science Program)", CS_MAJOR.getTitle());
        assertEquals(MAJOR, CS_MAJOR.getType());
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new ProgramResultItem(null, "t", MAJOR));
        assertThrows(NullPointerException.class, () -> new ProgramResultItem("c", null, MAJOR));
        assertThrows(NullPointerException.class, () -> new ProgramResultItem("c", "t", null));
    }

    @Test
    void toStringShowsCode() {
        assertTrue(CS_MAJOR.toString().contains("ASMAJ1689"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ProgramResultItem.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
