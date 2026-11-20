package use_case.check_eligibility;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckEligibilityInputDataTest {

    @Test
    void getterReturnsPlanId() {
        UUID id = UUID.randomUUID();

        assertEquals(id, new CheckEligibilityInputData(id).getPlanId());
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> new CheckEligibilityInputData(null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CheckEligibilityInputData.class).withNonnullFields("planId").verify();
    }

    @Test
    void toStringShowsPlanId() {
        UUID id = UUID.randomUUID();

        assertTrue(new CheckEligibilityInputData(id).toString().contains(id.toString()));
    }
}
