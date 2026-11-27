package entity.evaluation;

import entity.catalog.ProgramType;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProgramProgressTest {
    private static final EnrolmentResult ENROLMENT = new EnrolmentResult(Status.MET, "meets", null, List.of());
    private static final CompletionResult COMPLETION = new CompletionResult(List.of(), Status.NOT_MET);

    @Test
    void gettersReturnFields() {
        ProgramProgress p = new ProgramProgress("ASMAJ1689", ProgramType.MAJOR, ENROLMENT, COMPLETION);

        assertEquals("ASMAJ1689", p.getProgramCode());
        assertEquals(ProgramType.MAJOR, p.getType());
        assertSame(ENROLMENT, p.getEnrolment());
        assertSame(COMPLETION, p.getCompletion());
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(ProgramProgress.class)
                .withPrefabValues(EnrolmentResult.class, ENROLMENT,
                        new EnrolmentResult(Status.REVIEW, "review", null, List.of()))
                .withPrefabValues(CompletionResult.class, COMPLETION,
                        new CompletionResult(List.of(), Status.MET))
                .withNonnullFields("programCode", "type", "enrolment", "completion")
                .verify();
    }

    @Test
    void toStringShowsCodeAndStatuses() {
        ProgramProgress p = new ProgramProgress("ASMAJ1689", ProgramType.MAJOR, ENROLMENT, COMPLETION);

        assertEquals("ProgramProgress{ASMAJ1689 MAJOR, enrolment=MET, completion=NOT_MET}", p.toString());
    }
}
