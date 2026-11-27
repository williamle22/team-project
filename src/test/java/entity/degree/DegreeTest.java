package entity.degree;

import entity.catalog.Completion;
import entity.catalog.ProgramType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DegreeTest {
    private static final Completion EMPTY = new Completion(List.of(), List.of(), List.of(), List.of(), "");

    private static Degree degree(DegreeType type, int majors) {
        return new Degree(type, EMPTY, new ProgramCombinationRule(List.of(Map.of(ProgramType.MAJOR, majors))),
                List.of());
    }

    @Test
    void equalityIsByType() {
        assertEquals(degree(DegreeType.HBSC, 1), degree(DegreeType.HBSC, 2));
        assertEquals(degree(DegreeType.HBSC, 1).hashCode(), degree(DegreeType.HBSC, 2).hashCode());
        assertNotEquals(degree(DegreeType.HBSC, 2), degree(DegreeType.HBA, 2));
    }

    @Test
    void gettersReturnConstructorValues() {
        ProgramCombinationRule rule = new ProgramCombinationRule(List.of(Map.of(ProgramType.SPECIALIST, 1)));
        Degree d = new Degree(DegreeType.HBSC, EMPTY, rule, List.of("ASSPE2676"));
        assertEquals(DegreeType.HBSC, d.getType());
        assertEquals("Honours Bachelor of Science", d.getTitle());
        assertEquals(EMPTY, d.getCompletion());
        assertEquals(rule, d.getProgramCombinations());
        assertEquals(List.of("ASSPE2676"), d.getEligiblePrograms());
    }

    @Test
    void eligibleProgramsAreCopied() {
        List<String> codes = new ArrayList<>(List.of("ASSPE2676"));
        Degree d = new Degree(DegreeType.BCOM, EMPTY, degree(DegreeType.BCOM, 1).getProgramCombinations(), codes);

        codes.add("ASSPE2038");

        assertEquals(List.of("ASSPE2676"), d.getEligiblePrograms());
        assertThrows(UnsupportedOperationException.class, () -> d.getEligiblePrograms().add("ASSPE2038"));
    }

    @Test
    void emptyEligibleProgramsAcceptsAnyProgram() {
        assertTrue(degree(DegreeType.HBA, 2).countsTowardCombination("ASMAJ1689"));
    }

    @Test
    void eligibleProgramsLimitTheCombination() {
        Degree d = new Degree(DegreeType.BCOM, EMPTY, degree(DegreeType.BCOM, 1).getProgramCombinations(),
                List.of("ASSPE2676"));

        assertTrue(d.countsTowardCombination("ASSPE2676"));
        assertFalse(d.countsTowardCombination("ASSPE1689"));
    }

    @Test
    void nullEligibleProgramsThrows() {
        assertThrows(NullPointerException.class, () -> new Degree(DegreeType.HBA, EMPTY,
                degree(DegreeType.HBA, 2).getProgramCombinations(), null));
    }

    @Test
    void toStringShowsType() {
        assertTrue(degree(DegreeType.HBA, 2).toString().contains("HBA"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(Degree.class).suppress(Warning.NULL_FIELDS).withOnlyTheseFields("type").verify();
    }
}
