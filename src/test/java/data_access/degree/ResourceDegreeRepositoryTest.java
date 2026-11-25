package data_access.degree;

import entity.catalog.ProgramType;
import entity.degree.Degree;
import entity.degree.DegreeType;
import entity.evaluation.AcademicSnapshot;
import entity.evaluation.CompletionEvaluator;
import entity.evaluation.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.testDegree;

class ResourceDegreeRepositoryTest {
    private static final ResourceDegreeRepository REAL = new ResourceDegreeRepository();

    @Test
    void loadsTestDegree() {
        Degree d = new ResourceDegreeRepository("/fixtures/test-degree.json").findDegree(DegreeType.HBSC).orElseThrow();

        assertEquals(testDegree().getCompletion(), d.getCompletion());
        assertEquals(testDegree().getProgramCombinations(), d.getProgramCombinations());
    }

    @Test
    void unknownTypeIsEmpty() {
        assertTrue(new ResourceDegreeRepository("/fixtures/test-degree.json").findDegree(DegreeType.HBA).isEmpty());
    }

    @Test
    void missingFileThrows() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new ResourceDegreeRepository("/fixtures/nope.json"));

        assertEquals("missing degree file /fixtures/nope.json", e.getMessage());
    }

    @Test
    void allDegreesInTypeOrder() {
        List<Degree> all = REAL.allDegrees();

        assertEquals(List.of(DegreeType.HBA, DegreeType.HBSC, DegreeType.BCOM),
                List.of(all.get(0).getType(), all.get(1).getType(), all.get(2).getType()));
        assertThrows(UnsupportedOperationException.class, () -> all.add(testDegree()));
    }

    @Test
    void hbaAndHbscShareTheirRules() {
        Degree hba = REAL.findDegree(DegreeType.HBA).orElseThrow();
        Degree hbsc = REAL.findDegree(DegreeType.HBSC).orElseThrow();

        assertEquals(hba.getCompletion(), hbsc.getCompletion());
        assertEquals(List.of(Map.of(ProgramType.SPECIALIST, 1), Map.of(ProgramType.MAJOR, 2),
                Map.of(ProgramType.MAJOR, 1, ProgramType.MINOR, 2)), hba.getProgramCombinations().getCombinations());
        assertEquals(16, hba.getCompletion().getRequirements().size());
        assertTrue(hba.getEligiblePrograms().isEmpty());
    }

    @Test
    void bcomNeedsASpecialist() {
        Degree bcom = REAL.findDegree(DegreeType.BCOM).orElseThrow();

        assertEquals(List.of(Map.of(ProgramType.SPECIALIST, 1)), bcom.getProgramCombinations().getCombinations());
        assertEquals(19, bcom.getCompletion().getRequirements().size());
        assertEquals(List.of("ASSPE2038", "ASSPE2431", "ASSPE2676"), bcom.getEligiblePrograms());
    }

    @Test
    void everyDegreeFileEvaluates() {
        AcademicSnapshot empty = AcademicSnapshot.builder().build();

        for (Degree d : REAL.allDegrees()) {
            assertEquals(Status.NOT_MET, new CompletionEvaluator().evaluate(d.getCompletion(), empty).getOverall());
        }
    }
}
