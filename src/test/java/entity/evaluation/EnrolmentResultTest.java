package entity.evaluation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnrolmentResultTest {
    @Test
    void detailEmptyWhenNull() {
        EnrolmentResult r = new EnrolmentResult(Status.REVIEW, "no enrolment information", null, List.of());

        assertTrue(r.getDetail().isEmpty());
    }

    @Test
    void gettersReturnFields() {
        EvaluationNode detail = EvaluationNode.none("no requirements");
        EnrolmentResult r = new EnrolmentResult(Status.MET, "meets", detail, List.of("note"));

        assertEquals(Status.MET, r.getStatus());
        assertEquals("meets", r.getReason());
        assertEquals(detail, r.getDetail().orElseThrow());
        assertEquals(List.of("note"), r.getNotes());
    }

    @Test
    void notesAreCopied() {
        List<String> notes = new ArrayList<>(List.of("note"));
        EnrolmentResult r = new EnrolmentResult(Status.MET, "meets", null, notes);

        notes.add("later");

        assertEquals(List.of("note"), r.getNotes());
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(EnrolmentResult.class)
                .withPrefabValues(EvaluationNode.class, EvaluationNode.none("a"), EvaluationNode.none("b"))
                .withNonnullFields("status", "reason", "notes")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        EnrolmentResult r = new EnrolmentResult(Status.NOT_MET, "not admitting students", null, List.of("note"));

        assertEquals("EnrolmentResult{NOT_MET, not admitting students, notes=[note], detail=null}", r.toString());
    }
}
