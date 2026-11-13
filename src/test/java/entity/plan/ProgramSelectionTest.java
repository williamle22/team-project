package entity.plan;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramSelectionTest {

    @Test
    void withStatusCopies() {
        ProgramSelection intended = new ProgramSelection("ASMAJ1689", ProgramStatus.INTENDED);
        ProgramSelection enrolled = intended.withStatus(ProgramStatus.ENROLLED);
        assertEquals(ProgramStatus.INTENDED, intended.getStatus());
        assertEquals(new ProgramSelection("ASMAJ1689", ProgramStatus.ENROLLED), enrolled);
    }

    @Test
    void gettersReturnConstructorValues() {
        ProgramSelection p = new ProgramSelection("ASMAJ1689", ProgramStatus.ENROLLED);
        assertEquals("ASMAJ1689", p.getCode());
        assertEquals(ProgramStatus.ENROLLED, p.getStatus());
    }

    @Test
    void toStringShowsCode() {
        assertTrue(new ProgramSelection("ASMAJ1689", ProgramStatus.ENROLLED).toString().contains("ASMAJ1689"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ProgramSelection.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
