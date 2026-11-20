package entity.evaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExclusionResultTest {

    @Test
    void toStatus() {
        assertEquals(Status.NOT_MET, ExclusionResult.EXCLUDED.toStatus());
        assertEquals(Status.REVIEW, ExclusionResult.REVIEW.toStatus());
        assertEquals(Status.MET, ExclusionResult.NOT_EXCLUDED.toStatus());
    }
}
