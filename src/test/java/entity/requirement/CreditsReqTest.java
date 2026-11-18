package entity.requirement;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreditsReqTest {

    private static final CourseFilter HPS = CourseFilter.builder().designators(List.of("HPS")).build();

    @Test
    void gettersReturnConstructorValues() {
        CreditsReq r = new CreditsReq(1.0, 2.0, 0.5, HPS, true);
        assertEquals(1.0, r.getAmount());
        assertEquals(2.0, r.getMaxAmount().orElseThrow());
        assertEquals(0.5, r.getWeight().orElseThrow());
        assertEquals(HPS, r.getFilter());
        assertTrue(r.isConcurrent());
    }

    @Test
    void zeroMaxAmountIsNotAbsent() {
        assertEquals(0.0, new CreditsReq(0, 0.0, null, HPS, false).getMaxAmount().orElseThrow());
        assertTrue(new CreditsReq(0, null, null, HPS, false).getMaxAmount().isEmpty());
    }

    @Test
    void rejectsNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () -> new CreditsReq(-1.0, null, null, HPS, false));
    }

    @Test
    void rejectsNegativeMaxAmount() {
        assertThrows(IllegalArgumentException.class, () -> new CreditsReq(1.0, -1.0, null, HPS, false));
    }

    @Test
    void rejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class, () -> new CreditsReq(1.0, null, -1.0, HPS, false));
    }

    @Test
    void rejectsNullFilter() {
        assertThrows(NullPointerException.class, () -> new CreditsReq(1.0, null, null, null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new CreditsReq(1.0, null, null, HPS, false), new CreditsReq(1.0, null, null, HPS, false));
        assertEquals(new CreditsReq(1.0, null, null, HPS, false).hashCode(),
                new CreditsReq(1.0, null, null, HPS, false).hashCode());
        assertNotEquals(new CreditsReq(1.0, null, null, HPS, false), new CreditsReq(1.0, null, null, HPS, true));
        assertNotEquals(new CreditsReq(1.0, null, null, HPS, false), new CreditsReq(1.0, null, null, CourseFilter.any(), false));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(CreditsReq.class).usingGetClass().withRedefinedSuperclass()
                .suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void rejectsNonFiniteAmounts() {
        CourseFilter f = CourseFilter.builder().build();
        assertThrows(IllegalArgumentException.class, () -> new CreditsReq(Double.NaN, null, null, f, false));
        assertThrows(IllegalArgumentException.class, () -> new CreditsReq(1.0, Double.NaN, null, f, false));
        assertThrows(IllegalArgumentException.class, () -> new CreditsReq(1.0, null, Double.NaN, f, false));
    }
}
