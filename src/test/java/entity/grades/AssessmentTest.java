package entity.grades;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
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

    @Test
    void gettersReturnConstructorValues() {
        Assessment a = new Assessment("Midterm", 30, 85.0);
        assertEquals("Midterm", a.getName());
        assertEquals(30, a.getWeight());
        assertEquals(85.0, a.getScore().get());
    }

    @Test
    void toStringShowsScoreOnlyWhenGraded() {
        assertTrue(new Assessment("Midterm", 30, 85.0).toString().contains("85.0"));
        assertFalse(new Assessment("Midterm", 30, null).toString().contains("null"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Assessment.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void rejectsNonFiniteWeightOrScore() {
        assertThrows(IllegalArgumentException.class, () -> new Assessment("Quiz", Double.NaN, null));
        assertThrows(IllegalArgumentException.class, () -> new Assessment("Quiz", Double.POSITIVE_INFINITY, null));
        assertThrows(IllegalArgumentException.class, () -> new Assessment("Quiz", 10, Double.NaN));
    }
}
