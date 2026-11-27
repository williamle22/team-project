package entity.evaluation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static entity.evaluation.RowResultTest.row;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompletionResultTest {
    private static final RowResult ROW_1 = new RowResult(row(1), true, Status.MET, null, null, List.of());
    private static final RowResult ROW_2 = new RowResult(row(2), true, Status.NOT_MET, null, null, List.of());

    @Test
    void gettersReturnFields() {
        CompletionResult r = new CompletionResult(List.of(ROW_2, ROW_1), Status.NOT_MET);

        assertEquals(List.of(ROW_2, ROW_1), r.getRows());
        assertEquals(Status.NOT_MET, r.getOverall());
    }

    @Test
    void rowFindsByNumber() {
        CompletionResult r = new CompletionResult(List.of(ROW_2, ROW_1), Status.NOT_MET);

        assertSame(ROW_1, r.row(1).orElseThrow());
        assertSame(ROW_2, r.row(2).orElseThrow());
    }

    @Test
    void rowMissingIsEmpty() {
        CompletionResult r = new CompletionResult(List.of(ROW_1), Status.MET);

        assertTrue(r.row(7).isEmpty());
    }

    @Test
    void rowsAreCopied() {
        List<RowResult> rows = new ArrayList<>(List.of(ROW_1));
        CompletionResult r = new CompletionResult(rows, Status.MET);

        rows.add(ROW_2);

        assertEquals(List.of(ROW_1), r.getRows());
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CompletionResult.class)
                .withPrefabValues(RowResult.class, ROW_1, ROW_2)
                .withNonnullFields("rows", "overall")
                .verify();
    }

    @Test
    void toStringShowsOverallAndRows() {
        CompletionResult r = new CompletionResult(List.of(ROW_1), Status.MET);

        assertEquals("CompletionResult{MET, rows=[" + ROW_1 + "]}", r.toString());
    }
}
