package entity.degree;

import entity.catalog.Completion;
import entity.catalog.ProgramType;
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
}
