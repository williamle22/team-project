package entity.plan;

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
}
