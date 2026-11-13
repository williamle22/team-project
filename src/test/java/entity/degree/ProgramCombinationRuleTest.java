package entity.degree;

import entity.catalog.ProgramType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static entity.catalog.ProgramType.CERTIFICATE;
import static entity.catalog.ProgramType.FOCUS;
import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static entity.catalog.ProgramType.SPECIALIST;
import static org.junit.jupiter.api.Assertions.*;

class ProgramCombinationRuleTest {
    private static final ProgramCombinationRule ARTSCI = new ProgramCombinationRule(List.of(
            Map.of(SPECIALIST, 1),
            Map.of(MAJOR, 2),
            Map.of(MINOR, 2, MAJOR, 1)));

    @Test
    void oneSpecialist() {
        assertTrue(ARTSCI.satisfiedBy(List.of(SPECIALIST)));
    }

    @Test
    void twoMajors() {
        assertTrue(ARTSCI.satisfiedBy(List.of(MAJOR, MAJOR)));
    }

    @Test
    void majorAndMinorNotEnough() {
        assertFalse(ARTSCI.satisfiedBy(List.of(MAJOR, MINOR)));
    }

    @Test
    void threeMajorsStillSatisfy() {
        assertTrue(ARTSCI.satisfiedBy(List.of(MAJOR, MAJOR, MAJOR)));
    }

    @Test
    void majorAndTwoMinors() {
        assertTrue(ARTSCI.satisfiedBy(List.of(MINOR, MAJOR, MINOR)));
    }

    @Test
    void noProgramsNotEnough() {
        assertFalse(ARTSCI.satisfiedBy(List.of()));
    }

    @Test
    void describeReadsNaturally() {
        assertEquals("1 Specialist, or 2 Majors, or 1 Major and 2 Minors", ARTSCI.describe());
    }

    @Test
    void describePluralizesFocus() {
        ProgramCombinationRule rule = new ProgramCombinationRule(List.of(Map.of(FOCUS, 2, CERTIFICATE, 1)));
        assertEquals("2 Focuses and 1 Certificate", rule.describe());
    }

    @Test
    void copiesListAndMaps() {
        Map<ProgramType, Integer> majors = new HashMap<>(Map.of(MAJOR, 2));
        List<Map<ProgramType, Integer>> combinations = new ArrayList<>(List.of(majors));
        ProgramCombinationRule rule = new ProgramCombinationRule(combinations);
        majors.put(MAJOR, 1);
        combinations.add(Map.of(MINOR, 1));
        assertEquals(List.of(Map.of(MAJOR, 2)), rule.getCombinations());
        assertFalse(rule.satisfiedBy(List.of(MAJOR)));
    }

    @Test
    void rejectsEmptyAndZeroCounts() {
        assertThrows(IllegalArgumentException.class, () -> new ProgramCombinationRule(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ProgramCombinationRule(List.of(Map.of())));
        assertThrows(IllegalArgumentException.class, () -> new ProgramCombinationRule(List.of(Map.of(MAJOR, 0))));
    }

    @Test
    void toStringIncludesDescription() {
        assertTrue(ARTSCI.toString().contains("1 Specialist, or 2 Majors"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ProgramCombinationRule.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
