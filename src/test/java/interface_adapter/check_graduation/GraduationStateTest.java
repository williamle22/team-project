package interface_adapter.check_graduation;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraduationStateTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final GraduationLineView LINE = new GraduationLineView("Degree", "HBSc", "met", "", "", List.of());
    private static final ProgramView PROGRAM = new ProgramView("ASMAJ1689", "CS Major", "met", LINE, LINE, List.of());

    @Test
    void defaultsToEmpty() {
        GraduationState s = new GraduationState();

        assertNull(s.getPlanId());
        assertFalse(s.isProjected());
        assertEquals("", s.getStatus());
        assertEquals("", s.getSummary());
        assertNull(s.getDegree());
        assertNull(s.getCombination());
        assertEquals(List.of(), s.getNotes());
        assertEquals(List.of(), s.getPrograms());
        assertEquals("", s.getError());
    }

    @Test
    void settersUpdateFields() {
        GraduationState s = new GraduationState();

        s.setPlanId(ID);
        s.setProjected(true);
        s.setStatus("met");
        s.setSummary("Ready to graduate");
        s.setDegree(LINE);
        s.setCombination(LINE);
        s.setNotes(List.of("note"));
        s.setPrograms(List.of(PROGRAM));
        s.setError("error");

        assertEquals(ID, s.getPlanId());
        assertTrue(s.isProjected());
        assertEquals("met", s.getStatus());
        assertEquals("Ready to graduate", s.getSummary());
        assertEquals(LINE, s.getDegree());
        assertEquals(LINE, s.getCombination());
        assertEquals(List.of("note"), s.getNotes());
        assertEquals(List.of(PROGRAM), s.getPrograms());
        assertEquals("error", s.getError());
    }

    @Test
    void listsAreCopied() {
        List<String> notes = new ArrayList<>(List.of("note"));
        List<ProgramView> programs = new ArrayList<>(List.of(PROGRAM));
        GraduationState s = new GraduationState();

        s.setNotes(notes);
        s.setPrograms(programs);
        notes.clear();
        programs.clear();

        assertEquals(List.of("note"), s.getNotes());
        assertEquals(List.of(PROGRAM), s.getPrograms());
        assertThrows(UnsupportedOperationException.class, () -> s.getNotes().add("b"));
        assertThrows(UnsupportedOperationException.class, () -> s.getPrograms().add(PROGRAM));
    }

    @Test
    void toStringShowsModeCountAndError() {
        GraduationState s = new GraduationState();
        s.setPlanId(ID);
        s.setStatus("review");
        s.setSummary("Some requirements need review");
        s.setPrograms(List.of(PROGRAM));
        s.setNotes(List.of("note"));
        s.setError("e");

        assertEquals("GraduationState{" + ID + ", completed only, review, \"Some requirements need review\", "
                + "1 programs, notes=[note], error=e}", s.toString());
        s.setProjected(true);
        assertTrue(s.toString().contains(", whole plan, "));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(GraduationState.class)
                .withPrefabValues(GraduationLineView.class, LINE,
                        new GraduationLineView("b", "b", "review", "", "", List.of()))
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}
