package entity.requirement;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AverageReqTest {

    private static final CourseFilter ECO = CourseFilter.builder().designators(List.of("ECO")).build();
    private static final CreditsReq ITEM = new CreditsReq(1.0, null, null, ECO, false);

    @Test
    void gettersReturnConstructorValues() {
        AverageReq r = new AverageReq(73, 3.0, true, ECO, List.of(ITEM), true);
        assertEquals(73, r.getMinGrade());
        assertEquals(3.0, r.getAmount().orElseThrow());
        assertTrue(r.isBest());
        assertEquals(ECO, r.getFilter());
        assertEquals(List.of(ITEM), r.getItems());
        assertTrue(r.isConcurrent());
        assertTrue(new AverageReq(73, null, false, ECO, List.of(), false).getAmount().isEmpty());
    }

    @Test
    void copiesItems() {
        List<Requirement> items = new ArrayList<>();
        AverageReq r = new AverageReq(73, null, false, ECO, items, false);
        items.add(ITEM);
        assertTrue(r.getItems().isEmpty());
    }

    @Test
    void rejectsBadMinGrade() {
        assertThrows(IllegalArgumentException.class, () -> new AverageReq(101, null, false, ECO, List.of(), false));
        assertThrows(IllegalArgumentException.class, () -> new AverageReq(-1, null, false, ECO, List.of(), false));
    }

    @Test
    void rejectsFilterWithMinGrade() {
        CourseFilter withMin = CourseFilter.builder().designators(List.of("ECO")).minGrade(70).build();
        assertThrows(IllegalArgumentException.class, () -> new AverageReq(73, null, false, withMin, List.of(), false));
    }

    @Test
    void rejectsNullFilter() {
        assertThrows(NullPointerException.class, () -> new AverageReq(73, null, false, null, List.of(), false));
    }

    @Test
    void rejectsNullItems() {
        assertThrows(NullPointerException.class, () -> new AverageReq(73, null, false, ECO, null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new AverageReq(73, 3.0, false, ECO, List.of(ITEM), false),
                new AverageReq(73, 3.0, false, ECO, List.of(ITEM), false));
        assertEquals(new AverageReq(73, 3.0, false, ECO, List.of(ITEM), false).hashCode(),
                new AverageReq(73, 3.0, false, ECO, List.of(ITEM), false).hashCode());
        assertNotEquals(new AverageReq(73, 3.0, false, ECO, List.of(ITEM), false),
                new AverageReq(73, 3.0, false, ECO, List.of(ITEM), true));
        assertNotEquals(new AverageReq(73, 3.0, false, ECO, List.of(ITEM), false),
                new AverageReq(73, 3.0, true, ECO, List.of(ITEM), false));
    }
}
