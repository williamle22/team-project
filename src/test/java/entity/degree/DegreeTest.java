package entity.degree;

import entity.catalog.Completion;
import entity.catalog.ProgramType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DegreeTest {
    private static final Completion EMPTY = new Completion(List.of(), List.of(), List.of(), List.of(), "");

    private static Degree degree(DegreeType type, int majors) {
        return new Degree(type, EMPTY, new ProgramCombinationRule(List.of(Map.of(ProgramType.MAJOR, majors))));
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
        Degree d = new Degree(DegreeType.HBSC, EMPTY, rule);
        assertEquals(DegreeType.HBSC, d.getType());
        assertEquals("Honours Bachelor of Science", d.getTitle());
        assertEquals(EMPTY, d.getCompletion());
        assertEquals(rule, d.getProgramCombinations());
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
