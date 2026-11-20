package entity.evaluation;

import entity.catalog.Course;
import entity.catalog.Section;
import entity.term.Placement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.evaluation.OfferingStatus.NOT_APPLICABLE;
import static entity.evaluation.OfferingStatus.NOT_OFFERED;
import static entity.evaluation.OfferingStatus.OFFERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.withSessions;

class OfferingCheckTest {
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final Placement FULL_YEAR_2026 = Placement.fallWinter(2026, Section.Y);

    @Test
    void winterOnlyInFallNotOffered() {
        Course c = withSessions("CSC148H1", List.of(Section.S), List.of());

        assertEquals(NOT_OFFERED, OfferingCheck.check(c, FALL_2026, true));
    }

    @Test
    void winterOnlyInWinterOffered() {
        Course c = withSessions("CSC148H1", List.of(Section.S), List.of());

        assertEquals(OFFERED, OfferingCheck.check(c, WINTER_2027, true));
    }

    @Test
    void futureYearNotApplicable() {
        Course c = withSessions("CSC148H1", List.of(Section.S), List.of());

        assertEquals(NOT_APPLICABLE, OfferingCheck.check(c, FALL_2026, false));
        assertEquals(NOT_APPLICABLE, OfferingCheck.check(course("CSC148H1"), FALL_2026, false));
    }

    @Test
    void noSessionsNotOffered() {
        assertEquals(NOT_OFFERED, OfferingCheck.check(course("CSC148H1"), WINTER_2027, true));
    }

    @Test
    void summerSectionOffered() {
        Course c = withSessions("CSC148H1", List.of(Section.F, Section.S), List.of(Section.F));

        assertEquals(OFFERED, OfferingCheck.check(c, Placement.summer(2027, Section.F), true));
        assertEquals(NOT_OFFERED, OfferingCheck.check(c, Placement.summer(2027, Section.S), true));
    }

    @Test
    void fullYearNeedsY() {
        Course halves = withSessions("CSC148H1", List.of(Section.F, Section.S), List.of());
        Course full = withSessions("MAT137Y1", List.of(Section.Y), List.of());

        assertEquals(NOT_OFFERED, OfferingCheck.check(halves, FULL_YEAR_2026, true));
        assertEquals(OFFERED, OfferingCheck.check(full, FULL_YEAR_2026, true));
    }
}
