package entity.catalog;

import entity.requirement.ProgramReq;
import entity.requirement.Requirement;
import entity.requirement.RequirementVisitor;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnrolmentTest {

    private static final Requirement ANY = new Requirement(false) {
        @Override
        public <R> R accept(RequirementVisitor<R> visitor) {
            return null;
        }
    };

    @Test
    void suspendedHasNoReq() {
        Enrolment e = new Enrolment(EnrolmentKind.SUSPENDED, null, List.of(), false, List.of(), "");
        assertTrue(e.getReq().isEmpty());
    }

    @Test
    void openWithoutReqThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Enrolment(EnrolmentKind.OPEN, null, List.of(), false, List.of(), ""));
    }

    @Test
    void suspendedWithReqThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Enrolment(EnrolmentKind.SUSPENDED, ANY, List.of(), false, List.of(), ""));
    }

    @Test
    void openKeepsReq() {
        Enrolment e = new Enrolment(EnrolmentKind.OPEN, ANY, List.of(), true, List.of(), "raw");
        assertEquals(ANY, e.getReq().orElseThrow());
        assertTrue(e.isFirstRoundOnly());
    }

    @Test
    void gettersReturnConstructorValues() {
        ProgramReq major = new ProgramReq(List.of("ASMAJ1001"), "ASMAJ1001", false);
        Enrolment e = new Enrolment(EnrolmentKind.LIMITED, ANY, List.of(major), false,
                List.of("Apply in the first round"), "raw");
        assertEquals(EnrolmentKind.LIMITED, e.getKind());
        assertEquals(List.of(major), e.getExclusions());
        assertFalse(e.isFirstRoundOnly());
        assertEquals(List.of("Apply in the first round"), e.getNotes());
        assertEquals("raw", e.getRaw());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Enrolment.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesKind() {
        assertTrue(new Enrolment(EnrolmentKind.SUSPENDED, null, List.of(), false, List.of(), "").toString()
                .contains("SUSPENDED"));
    }
}
