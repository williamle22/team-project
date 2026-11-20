package use_case;

import entity.evaluation.AcademicSnapshot;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotResultTest {
    private static final AcademicSnapshot SNAPSHOT = AcademicSnapshot.builder().build();

    @Test
    void gettersReturnConstructorValues() {
        SnapshotResult r = new SnapshotResult(SNAPSHOT, List.of("XYZ123H1 isn't in the current calendar"));

        assertSame(SNAPSHOT, r.getSnapshot());
        assertEquals(List.of("XYZ123H1 isn't in the current calendar"), r.getWarnings());
    }

    @Test
    void copiesWarnings() {
        List<String> warnings = new ArrayList<>(List.of("a"));
        SnapshotResult r = new SnapshotResult(SNAPSHOT, warnings);

        warnings.add("b");

        assertEquals(List.of("a"), r.getWarnings());
        assertThrows(UnsupportedOperationException.class, () -> r.getWarnings().add("c"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new SnapshotResult(null, List.of()));
        assertThrows(NullPointerException.class, () -> new SnapshotResult(SNAPSHOT, null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(SnapshotResult.class).withNonnullFields("snapshot", "warnings").verify();
    }

    @Test
    void toStringShowsFields() {
        String s = new SnapshotResult(SNAPSHOT, List.of("XYZ123H1 isn't in the current calendar")).toString();

        assertTrue(s.contains("AcademicSnapshot"));
        assertTrue(s.contains("XYZ123H1 isn't in the current calendar"));
    }
}
