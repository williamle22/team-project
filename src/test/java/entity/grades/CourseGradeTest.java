package entity.grades;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseGradeTest {
    private static final double DELTA = 1e-9;

    private static CourseGrade partlyGraded() {
        return new CourseGrade(null, List.of(
                new Assessment("Quizzes", 20, 80.0),
                new Assessment("Midterm", 30, 90.0),
                new Assessment("Final", 50, null)));
    }

    @Test
    void emptyHasNoPredictions() {
        CourseGrade g = CourseGrade.empty();
        assertTrue(g.currentAverage().isEmpty());
        assertTrue(g.guaranteedMinimum().isEmpty());
        assertTrue(g.neededOnRemaining(80).isEmpty());
    }

    @Test
    void weightsSplitByGraded() {
        CourseGrade g = partlyGraded();
        assertEquals(100, g.totalWeight(), DELTA);
        assertEquals(50, g.gradedWeight(), DELTA);
        assertEquals(50, g.remainingWeight(), DELTA);
    }

    @Test
    void currentAverageOverGradedOnly() {
        assertEquals(86.0, partlyGraded().currentAverage().getAsDouble(), DELTA);
    }

    @Test
    void guaranteedMinimumAssumesZeros() {
        assertEquals(43.0, partlyGraded().guaranteedMinimum().getAsDouble(), DELTA);
    }

    @Test
    void neededOnRemaining() {
        CourseGrade g = partlyGraded();
        assertEquals(74.0, g.neededOnRemaining(80).getAsDouble(), DELTA);
        assertEquals(114.0, g.neededOnRemaining(100).getAsDouble(), DELTA);
        assertEquals(-6.0, g.neededOnRemaining(40).getAsDouble(), DELTA);
    }

    @Test
    void neededOnRemainingEmptyWhenAllGraded() {
        CourseGrade g = partlyGraded().withAssessmentReplaced(2, new Assessment("Final", 50, 70.0));
        assertTrue(g.neededOnRemaining(80).isEmpty());
    }

    @Test
    void weightWarningWhenNotHundred() {
        CourseGrade under = partlyGraded().withAssessmentReplaced(2, new Assessment("Final", 40, null));
        assertEquals("Assessment weights total 90.00%, not 100%.", under.weightWarning().get());
        assertTrue(partlyGraded().weightWarning().isEmpty());
        assertTrue(CourseGrade.empty().weightWarning().isEmpty());
    }

    @Test
    void rejectsFinalGradeOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> CourseGrade.empty().withFinalGrade(101));
        assertThrows(IllegalArgumentException.class, () -> CourseGrade.empty().withFinalGrade(-1));
    }

    @Test
    void withMethodsDontMutate() {
        CourseGrade original = partlyGraded();
        CourseGrade graded = original.withFinalGrade(85);
        assertFalse(original.hasFinalGrade());
        assertEquals(85, graded.getFinalGrade().get());
        assertEquals(original.getAssessments(), graded.getAssessments());
    }

    @Test
    void editsAssessmentList() {
        Assessment lab = new Assessment("Lab", 10, null);
        CourseGrade g = partlyGraded();
        assertEquals(lab, g.withAssessmentAdded(lab).getAssessments().get(3));
        assertEquals(lab, g.withAssessmentReplaced(0, lab).getAssessments().get(0));
        assertEquals("Midterm", g.withAssessmentRemoved(0).getAssessments().get(0).getName());
        assertEquals(3, g.getAssessments().size());
    }
}
