package entity.evaluation;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.req;

class EligibilityResultTest {
    private static final EvaluationNode MET_NODE = EvaluationNode.none("no prerequisites");
    private static final ExclusionOutcome NOT_EXCLUDED =
            new ExclusionOutcome(ExclusionResult.NOT_EXCLUDED, List.of(), List.of());

    private static EvaluationNode node(Status s) {
        return EvaluationNode.leaf(req("CSC148H1"), s, null);
    }

    private static ExclusionOutcome exclusion(ExclusionResult r) {
        return new ExclusionOutcome(r, List.of(), List.of());
    }

    private static Status overall(EvaluationNode prereqs, EvaluationNode coreqs, ExclusionOutcome exclusions,
                                  OfferingStatus offering) {
        return EligibilityResult.of("CSC207H1", prereqs, coreqs, exclusions, offering, List.of()).getOverall();
    }

    @Test
    void overallCombinesParts() {
        assertEquals(MET, overall(MET_NODE, MET_NODE, NOT_EXCLUDED, OfferingStatus.OFFERED));
        assertEquals(MET, overall(MET_NODE, MET_NODE, NOT_EXCLUDED, OfferingStatus.NOT_APPLICABLE));

        assertEquals(NOT_MET, overall(node(NOT_MET), MET_NODE, NOT_EXCLUDED, OfferingStatus.OFFERED));
        assertEquals(REVIEW, overall(node(REVIEW), MET_NODE, NOT_EXCLUDED, OfferingStatus.OFFERED));
        assertEquals(NOT_MET, overall(MET_NODE, node(NOT_MET), NOT_EXCLUDED, OfferingStatus.OFFERED));
        assertEquals(REVIEW, overall(MET_NODE, node(REVIEW), NOT_EXCLUDED, OfferingStatus.OFFERED));
        assertEquals(NOT_MET, overall(MET_NODE, MET_NODE, exclusion(ExclusionResult.EXCLUDED),
                OfferingStatus.OFFERED));
        assertEquals(REVIEW, overall(MET_NODE, MET_NODE, exclusion(ExclusionResult.REVIEW), OfferingStatus.OFFERED));
        assertEquals(NOT_MET, overall(MET_NODE, MET_NODE, NOT_EXCLUDED, OfferingStatus.NOT_OFFERED));

        assertEquals(NOT_MET, overall(node(REVIEW), MET_NODE, NOT_EXCLUDED, OfferingStatus.NOT_OFFERED));
    }

    @Test
    void gettersReturnConstructorValues() {
        EvaluationNode coreqs = EvaluationNode.none("no corequisites");
        EligibilityResult r = EligibilityResult.of("CSC207H1", MET_NODE, coreqs, NOT_EXCLUDED,
                OfferingStatus.NOT_OFFERED, List.of("not offered in Winter 2027"));

        assertEquals("CSC207H1", r.getCourseCode());
        assertEquals(MET_NODE, r.getPrerequisites());
        assertEquals(coreqs, r.getCorequisites());
        assertEquals(NOT_EXCLUDED, r.getExclusions());
        assertEquals(OfferingStatus.NOT_OFFERED, r.getOffering());
        assertEquals(List.of("not offered in Winter 2027"), r.getWarnings());
    }

    @Test
    void copiesWarnings() {
        List<String> warnings = new ArrayList<>(List.of("a"));
        EligibilityResult r = EligibilityResult.of("CSC207H1", MET_NODE, MET_NODE, NOT_EXCLUDED,
                OfferingStatus.OFFERED, warnings);

        warnings.add("b");

        assertEquals(List.of("a"), r.getWarnings());
        assertThrows(UnsupportedOperationException.class, () -> r.getWarnings().add("c"));
    }

    @Test
    void rejectsNulls() {
        OfferingStatus o = OfferingStatus.OFFERED;

        assertThrows(NullPointerException.class,
                () -> EligibilityResult.of(null, MET_NODE, MET_NODE, NOT_EXCLUDED, o, List.of()));
        assertThrows(NullPointerException.class,
                () -> EligibilityResult.of("CSC207H1", null, MET_NODE, NOT_EXCLUDED, o, List.of()));
        assertThrows(NullPointerException.class,
                () -> EligibilityResult.of("CSC207H1", MET_NODE, null, NOT_EXCLUDED, o, List.of()));
        assertThrows(NullPointerException.class,
                () -> EligibilityResult.of("CSC207H1", MET_NODE, MET_NODE, null, o, List.of()));
        assertThrows(NullPointerException.class,
                () -> EligibilityResult.of("CSC207H1", MET_NODE, MET_NODE, NOT_EXCLUDED, null, List.of()));
        assertThrows(NullPointerException.class,
                () -> EligibilityResult.of("CSC207H1", MET_NODE, MET_NODE, NOT_EXCLUDED, o, null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(EligibilityResult.class)
                .withPrefabValues(EvaluationNode.class, EvaluationNode.none("a"), EvaluationNode.none("b"))
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }

    @Test
    void toStringShowsFields() {
        String s = EligibilityResult.of("CSC207H1", MET_NODE, MET_NODE, NOT_EXCLUDED, OfferingStatus.NOT_OFFERED,
                List.of("not offered in Winter 2027")).toString();

        assertTrue(s.contains("CSC207H1"));
        assertTrue(s.contains("NOT_MET"));
        assertTrue(s.contains("NOT_OFFERED"));
        assertTrue(s.contains("not offered in Winter 2027"));
    }
}
