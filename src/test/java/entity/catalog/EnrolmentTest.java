package entity.catalog;

import entity.requirement.Requirement;
import entity.requirement.RequirementVisitor;
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
}