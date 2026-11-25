package use_case.check_graduation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProgramSummaryTest {
    private static final RowLine ENROLMENT = new RowLine("Enrolment", "open", MET, null, List.of(), List.of());
    private static final RowLine COMPLETION = new RowLine("Completion", "4.0 credits", REVIEW, null, List.of(),
            List.of());

    @Test
    void gettersReturnConstructorValues() {
        ProgramSummary p = new ProgramSummary("ASMIN1001", "Bioethics Minor", ENROLMENT, COMPLETION,
                List.of("note"));

        assertEquals("ASMIN1001", p.getCode());
        assertEquals("Bioethics Minor", p.getTitle());
        assertSame(ENROLMENT, p.getEnrolment());
        assertSame(COMPLETION, p.getCompletion());
        assertEquals(List.of("note"), p.getNotes());
    }

    @Test
    void copiesNotes() {
        List<String> notes = new ArrayList<>(List.of("note"));
        ProgramSummary p = new ProgramSummary("ASMIN1001", "t", ENROLMENT, COMPLETION, notes);

        notes.add("other");

        assertEquals(List.of("note"), p.getNotes());
        assertThrows(UnsupportedOperationException.class, () -> p.getNotes().add("other"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> new ProgramSummary(null, "t", ENROLMENT, COMPLETION, List.of()));
        assertThrows(NullPointerException.class,
                () -> new ProgramSummary("c", null, ENROLMENT, COMPLETION, List.of()));
        assertThrows(NullPointerException.class, () -> new ProgramSummary("c", "t", null, COMPLETION, List.of()));
        assertThrows(NullPointerException.class, () -> new ProgramSummary("c", "t", ENROLMENT, null, List.of()));
        assertThrows(NullPointerException.class, () -> new ProgramSummary("c", "t", ENROLMENT, COMPLETION, null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(ProgramSummary.class)
                .withPrefabValues(RowLine.class, ENROLMENT, COMPLETION)
                .withNonnullFields("code", "title", "enrolment", "completion", "notes")
                .verify();
    }

    @Test
    void toStringShowsStatuses() {
        ProgramSummary p = new ProgramSummary("ASMIN1001", "Bioethics Minor", ENROLMENT, COMPLETION,
                List.of("note"));

        assertEquals("ProgramSummary{ASMIN1001 \"Bioethics Minor\", enrolment=MET, completion=REVIEW, "
                + "notes=[note]}", p.toString());
    }
}
