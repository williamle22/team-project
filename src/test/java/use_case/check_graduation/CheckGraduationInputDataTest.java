package use_case.check_graduation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckGraduationInputDataTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void gettersReturnConstructorValues() {
        CheckGraduationInputData input = new CheckGraduationInputData(ID, true);

        assertEquals(ID, input.getPlanId());
        assertTrue(input.isProjected());
        assertFalse(new CheckGraduationInputData(ID, false).isProjected());
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> new CheckGraduationInputData(null, true));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CheckGraduationInputData.class).withNonnullFields("planId").verify();
    }

    @Test
    void toStringShowsMode() {
        assertEquals("CheckGraduationInputData{" + ID + ", whole plan}",
                new CheckGraduationInputData(ID, true).toString());
        assertEquals("CheckGraduationInputData{" + ID + ", completed only}",
                new CheckGraduationInputData(ID, false).toString());
    }
}
