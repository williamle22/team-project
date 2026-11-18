package entity.catalog;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;
import testutil.CatalogFixtures;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramTest {

    @Test
    void equalityIsByCode() {
        Program a = new Program("ASMIN1001", ProgramType.MINOR, "1001", "History and Philosophy of Science",
                null, null, List.of());
        Program b = new Program("ASMIN1001", ProgramType.MINOR, "1001", "A different title", null, null, List.of());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Program("ASMAJ1001", ProgramType.MAJOR, "1001", "History and Philosophy of Science",
                null, null, List.of()));
    }

    @Test
    void gettersReturnConstructorValues() {
        Enrolment enrolment = new Enrolment(EnrolmentKind.OPEN, CatalogFixtures.req("HPS100H1"), List.of(), false,
                List.of(), "");
        Completion completion = new Completion(List.of(), List.of(), List.of(), List.of(), "");
        Program p = new Program("ASMIN1001", ProgramType.MINOR, "1001", "History and Philosophy of Science",
                enrolment, completion, List.of("History and Philosophy of Science"));
        assertEquals("ASMIN1001", p.getCode());
        assertEquals(ProgramType.MINOR, p.getType());
        assertEquals("1001", p.getNumber());
        assertEquals("History and Philosophy of Science", p.getTitle());
        assertEquals(enrolment, p.getEnrolment().orElseThrow());
        assertEquals(completion, p.getCompletion().orElseThrow());
        assertEquals(List.of("History and Philosophy of Science"), p.getAreas());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Program.class).withOnlyTheseFields("code").suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesCode() {
        assertTrue(CatalogFixtures.program("ASMIN1001", ProgramType.MINOR, null).toString().contains("ASMIN1001"));
    }
}
