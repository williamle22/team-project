package entity.evaluation;

import entity.catalog.ProgramType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static entity.catalog.ProgramType.MAJOR;
import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.record;
import static testutil.CatalogFixtures.testDegree;

class DegreeEvaluatorTest {
    private static final String[] THREE_CREDITS = {
            "CSC108H1", "CSC148H1", "CSC207H1", "CSC236H1", "CSC369H1", "CSC373H1"};

    private final DegreeEvaluator evaluator = new DegreeEvaluator(new CompletionEvaluator());

    private static AcademicSnapshot taken(String... codes) {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder();
        for (String code : codes) {
            b.taken(record(code));
        }
        return b.build();
    }

    private DegreeProgress evaluate(AcademicSnapshot snapshot, List<ProgramType> enrolled,
                                    List<ProgramType> intended) {
        return evaluator.evaluate(testDegree(), snapshot, enrolled, intended);
    }

    @Test
    void met() {
        DegreeProgress p = evaluate(taken(THREE_CREDITS), List.of(MAJOR, MAJOR), List.of());

        assertEquals(testDegree().getType(), p.getType());
        assertEquals(MET, p.getCompletion().getOverall());
        assertEquals(MET, p.getProgramCombination());
        assertTrue(p.getProgramCombinationReason().isEmpty());
        assertEquals(MET, p.getOverall());
    }

    @Test
    void notEnoughCredits() {
        DegreeProgress p = evaluate(taken("CSC108H1", "CSC148H1", "CSC207H1", "CSC369H1", "CSC373H1"),
                List.of(MAJOR, MAJOR), List.of());

        assertEquals(NOT_MET, p.getCompletion().getOverall());
        assertEquals(MET, p.getProgramCombination());
        assertEquals(NOT_MET, p.getOverall());
    }

    @Test
    void tooMuchFirstYear() {
        DegreeProgress p = evaluate(taken("CSC108H1", "CSC148H1", "CSC165H1", "CSC207H1", "CSC369H1", "CSC373H1"),
                List.of(MAJOR, MAJOR), List.of());

        assertEquals(NOT_MET, p.getCompletion().row(3).orElseThrow().getStatus());
        assertEquals(NOT_MET, p.getCompletion().getOverall());
        assertEquals(NOT_MET, p.getOverall());
    }

    @Test
    void intendedProgramGivesReview() {
        DegreeProgress p = evaluate(taken(THREE_CREDITS), List.of(MAJOR), List.of(MAJOR));

        assertEquals(REVIEW, p.getProgramCombination());
        assertEquals("met once you're admitted to your intended programs",
                p.getProgramCombinationReason().orElseThrow());
        assertEquals(REVIEW, p.getOverall());
    }

    @Test
    void combinationNotMetNamesTheRule() {
        DegreeProgress p = evaluate(taken(THREE_CREDITS), List.of(MAJOR), List.of());

        assertEquals(NOT_MET, p.getProgramCombination());
        assertEquals("needs 2 Majors", p.getProgramCombinationReason().orElseThrow());
        assertEquals(NOT_MET, p.getOverall());
    }

    @Test
    void callerListsUnchanged() {
        List<ProgramType> enrolled = new ArrayList<>(List.of(MAJOR));

        evaluate(taken(), enrolled, List.of(MAJOR));

        assertEquals(List.of(MAJOR), enrolled);
    }
}
