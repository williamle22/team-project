package entity.evaluation;

import entity.degree.DegreeType;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DegreeProgressTest {
    private static CompletionResult rows(Status overall) {
        return new CompletionResult(List.of(), overall, List.of());
    }

    @Test
    void overallCombinesRowsAndCombination() {
        assertEquals(MET, new DegreeProgress(DegreeType.HBA, rows(MET), MET, null).getOverall());
        assertEquals(REVIEW, new DegreeProgress(DegreeType.HBA, rows(MET), REVIEW, "r").getOverall());
        assertEquals(REVIEW, new DegreeProgress(DegreeType.HBA, rows(REVIEW), MET, null).getOverall());
        assertEquals(NOT_MET, new DegreeProgress(DegreeType.HBA, rows(NOT_MET), REVIEW, "r").getOverall());
        assertEquals(NOT_MET, new DegreeProgress(DegreeType.HBA, rows(MET), NOT_MET, "r").getOverall());
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(DegreeProgress.class)
                .withPrefabValues(CompletionResult.class, rows(MET), rows(NOT_MET))
                .withNonnullFields("type", "completion", "programCombination")
                .verify();
    }

    @Test
    void toStringShowsStatuses() {
        DegreeProgress p = new DegreeProgress(DegreeType.BCOM, rows(MET), REVIEW, "r");

        assertEquals("DegreeProgress{BCOM, REVIEW, completion=MET, programCombination=REVIEW}", p.toString());
    }
}
