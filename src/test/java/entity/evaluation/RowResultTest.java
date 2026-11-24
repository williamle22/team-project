package entity.evaluation;

import entity.catalog.CompletionRow;
import entity.catalog.RowKind;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.req;

class RowResultTest {
    private static final CompletionRow ROW_3 = row(3);

    static CompletionRow row(int number) {
        return new CompletionRow(number, RowKind.ALLOCATION, null, null, null, List.of(), req("CSC148H1"), null,
                false, List.of(), "raw " + number);
    }

    @Test
    void gettersReturnFields() {
        EvaluationNode detail = EvaluationNode.none("no requirements");
        RowResult r = new RowResult(ROW_3, true, Status.MET, "reason", detail, List.of("CSC148H1"));

        assertSame(ROW_3, r.getRow());
        assertEquals(3, r.getNumber());
        assertTrue(r.isApplicable());
        assertEquals(Status.MET, r.getStatus());
        assertEquals("reason", r.getReason().orElseThrow());
        assertEquals(detail, r.getDetail().orElseThrow());
        assertEquals(List.of("CSC148H1"), r.getClaimed());
    }

    @Test
    void reasonAndDetailEmptyWhenNull() {
        RowResult r = new RowResult(ROW_3, false, Status.NOT_MET, null, null, List.of());

        assertFalse(r.isApplicable());
        assertTrue(r.getReason().isEmpty());
        assertTrue(r.getDetail().isEmpty());
    }

    @Test
    void claimedIsCopied() {
        List<String> claimed = new ArrayList<>(List.of("CSC148H1"));
        RowResult r = new RowResult(ROW_3, true, Status.MET, null, null, claimed);

        claimed.add("CSC207H1");

        assertEquals(List.of("CSC148H1"), r.getClaimed());
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(RowResult.class)
                .withPrefabValues(CompletionRow.class, row(1), row(2))
                .withPrefabValues(EvaluationNode.class, EvaluationNode.none("a"), EvaluationNode.none("b"))
                .withNonnullFields("row", "status", "claimed")
                .verify();
    }

    @Test
    void toStringShowsNumberStatusAndClaimed() {
        RowResult r = new RowResult(ROW_3, true, Status.MET, "reason", null, List.of("CSC148H1"));

        assertEquals("RowResult{3, MET, applicable=true, reason=reason, claimed=[CSC148H1]}", r.toString());
    }
}
