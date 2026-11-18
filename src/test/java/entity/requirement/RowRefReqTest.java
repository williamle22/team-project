package entity.requirement;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RowRefReqTest {

    @Test
    void gettersReturnConstructorValues() {
        RowRefReq r = new RowRefReq(List.of(3, 4), true);
        assertEquals(List.of(3, 4), r.getRows());
        assertTrue(r.isConcurrent());
    }

    @Test
    void copiesRows() {
        List<Integer> rows = new ArrayList<>(List.of(3));
        RowRefReq r = new RowRefReq(rows, false);
        rows.add(4);
        assertEquals(List.of(3), r.getRows());
    }

    @Test
    void rejectsEmptyRows() {
        assertThrows(IllegalArgumentException.class, () -> new RowRefReq(List.of(), false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new RowRefReq(List.of(3, 4), false), new RowRefReq(List.of(3, 4), false));
        assertEquals(new RowRefReq(List.of(3, 4), false).hashCode(), new RowRefReq(List.of(3, 4), false).hashCode());
        assertNotEquals(new RowRefReq(List.of(3, 4), false), new RowRefReq(List.of(3, 4), true));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(RowRefReq.class).usingGetClass().withRedefinedSuperclass()
                .suppress(Warning.NULL_FIELDS).verify();
    }
}
