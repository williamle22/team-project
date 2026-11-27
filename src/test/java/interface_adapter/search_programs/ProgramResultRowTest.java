package interface_adapter.search_programs;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramResultRowTest {

    @Test
    void gettersReturnConstructorValues() {
        ProgramResultRow row = new ProgramResultRow("ASMAJ1689", "Computer Science Major (Science Program)", "Major");
        assertEquals("ASMAJ1689", row.getCode());
        assertEquals("Computer Science Major (Science Program)", row.getTitle());
        assertEquals("Major", row.getType());
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new ProgramResultRow(null, "t", "Major"));
        assertThrows(NullPointerException.class, () -> new ProgramResultRow("c", null, "Major"));
        assertThrows(NullPointerException.class, () -> new ProgramResultRow("c", "t", null));
    }

    @Test
    void toStringShowsCode() {
        assertTrue(new ProgramResultRow("ASMAJ1689", "t", "Major").toString().contains("ASMAJ1689"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ProgramResultRow.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
