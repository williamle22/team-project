package interface_adapter.check_graduation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProgramViewTest {
    private static final GraduationLineView ENROLMENT =
            new GraduationLineView("Enrolment", "Open enrolment.", "met", "", "", List.of());
    private static final GraduationLineView COMPLETION =
            new GraduationLineView("Completion", "Completion requirements", "review", "", "", List.of());

    @Test
    void gettersReturnConstructorValues() {
        ProgramView v = new ProgramView("ASMIN1001", "Bioethics Minor", "review", ENROLMENT, COMPLETION,
                List.of("note"));

        assertEquals("ASMIN1001", v.getCode());
        assertEquals("Bioethics Minor", v.getTitle());
        assertEquals("review", v.getStatus());
        assertSame(ENROLMENT, v.getEnrolment());
        assertSame(COMPLETION, v.getCompletion());
        assertEquals(List.of("note"), v.getNotes());
    }

    @Test
    void copiesNotes() {
        List<String> notes = new ArrayList<>(List.of("note"));
        ProgramView v = new ProgramView("c", "t", "met", ENROLMENT, COMPLETION, notes);

        notes.add("other");

        assertEquals(List.of("note"), v.getNotes());
        assertThrows(UnsupportedOperationException.class, () -> v.getNotes().add("other"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> new ProgramView(null, "t", "met", ENROLMENT, COMPLETION, List.of()));
        assertThrows(NullPointerException.class,
                () -> new ProgramView("c", null, "met", ENROLMENT, COMPLETION, List.of()));
        assertThrows(NullPointerException.class,
                () -> new ProgramView("c", "t", null, ENROLMENT, COMPLETION, List.of()));
        assertThrows(NullPointerException.class, () -> new ProgramView("c", "t", "met", null, COMPLETION, List.of()));
        assertThrows(NullPointerException.class, () -> new ProgramView("c", "t", "met", ENROLMENT, null, List.of()));
        assertThrows(NullPointerException.class, () -> new ProgramView("c", "t", "met", ENROLMENT, COMPLETION, null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(ProgramView.class)
                .withPrefabValues(GraduationLineView.class, ENROLMENT, COMPLETION)
                .withNonnullFields("code", "title", "status", "enrolment", "completion", "notes")
                .verify();
    }

    @Test
    void toStringShowsCodeTitleAndStatus() {
        ProgramView v = new ProgramView("ASMIN1001", "Bioethics Minor", "review", ENROLMENT, COMPLETION,
                List.of("note"));

        assertEquals("ProgramView{ASMIN1001 \"Bioethics Minor\", review, notes=[note]}", v.toString());
    }
}
