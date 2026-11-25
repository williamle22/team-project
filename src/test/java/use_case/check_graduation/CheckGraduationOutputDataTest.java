package use_case.check_graduation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckGraduationOutputDataTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final RowLine DEGREE = new RowLine("Degree", "Honours Bachelor of Science", NOT_MET, null,
            List.of(), List.of());
    private static final RowLine COMBINATION = new RowLine("Programs", "1 Specialist", MET, null, List.of(),
            List.of());
    private static final ProgramSummary PROGRAM = new ProgramSummary("ASSPE1689", "CS Specialist", COMBINATION,
            COMBINATION, List.of());

    private static CheckGraduationOutputData output(boolean projected, List<String> notes,
                                                    List<ProgramSummary> programs) {
        return new CheckGraduationOutputData(ID, projected, REVIEW, DEGREE, COMBINATION, notes, programs);
    }

    @Test
    void gettersReturnConstructorValues() {
        CheckGraduationOutputData o = output(true, List.of("note"), List.of(PROGRAM));

        assertEquals(ID, o.getPlanId());
        assertTrue(o.isProjected());
        assertFalse(output(false, List.of(), List.of()).isProjected());
        assertEquals(REVIEW, o.getOverall());
        assertSame(DEGREE, o.getDegree());
        assertSame(COMBINATION, o.getCombination());
        assertEquals(List.of("note"), o.getNotes());
        assertEquals(List.of(PROGRAM), o.getPrograms());
    }

    @Test
    void copiesNotesAndPrograms() {
        List<String> notes = new ArrayList<>(List.of("note"));
        List<ProgramSummary> programs = new ArrayList<>(List.of(PROGRAM));
        CheckGraduationOutputData o = output(true, notes, programs);

        notes.add("other");
        programs.add(PROGRAM);

        assertEquals(List.of("note"), o.getNotes());
        assertEquals(List.of(PROGRAM), o.getPrograms());
        assertThrows(UnsupportedOperationException.class, () -> o.getNotes().add("other"));
        assertThrows(UnsupportedOperationException.class, () -> o.getPrograms().add(PROGRAM));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new CheckGraduationOutputData(null, true, MET, DEGREE,
                COMBINATION, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new CheckGraduationOutputData(ID, true, null, DEGREE,
                COMBINATION, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new CheckGraduationOutputData(ID, true, MET, null,
                COMBINATION, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new CheckGraduationOutputData(ID, true, MET, DEGREE,
                null, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> output(true, null, List.of()));
        assertThrows(NullPointerException.class, () -> output(true, List.of(), null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(CheckGraduationOutputData.class)
                .withPrefabValues(RowLine.class, DEGREE, COMBINATION)
                .withNonnullFields("planId", "overall", "degree", "combination", "notes", "programs")
                .verify();
    }

    @Test
    void toStringShowsModeAndStatuses() {
        assertEquals("CheckGraduationOutputData{" + ID + ", whole plan, REVIEW, degree=NOT_MET, combination=MET, "
                + "programs=[], notes=[note]}", output(true, List.of("note"), List.of()).toString());
        assertTrue(output(false, List.of(), List.of()).toString().contains(", completed only, "));
    }
}
