package entity.grades;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentTest {

    @Test
    void rejectsZeroWeight() {
        assertThrows(IllegalArgumentException.class, () -> new Assessment("Quiz", 0, null));
    }

    @Test
    void rejectsNegativeScore() {
        assertThrows(IllegalArgumentException.class, () -> new Assessment("Quiz", 10, -1.0));
    }

    @Test
    void allowsBonusAboveHundred() {
        assertEquals(105.0, new Assessment("Quiz", 10, 105.0).getScore().get());
    }

    @Test
    void withScoreGrades() {
        Assessment ungraded = new Assessment("Quiz", 10, null);
        Assessment graded = ungraded.withScore(80.0);
        assertFalse(ungraded.isGraded());
        assertTrue(graded.isGraded());
        assertEquals(80.0, graded.getScore().get());
    }
}
