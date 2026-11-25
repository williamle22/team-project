package use_case.check_graduation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RowLineTest {
    private static final RowLine CHILD = new RowLine("1", "PHL281H1", MET, null, List.of("PHL281H1"), List.of());

    @Test
    void gettersReturnConstructorValues() {
        RowLine line = new RowLine("Requirement 1", "4.0 credits", NOT_MET, "3.5 of 4.0 credits",
                List.of("PHL281H1"), List.of(CHILD));

        assertEquals("Requirement 1", line.getLabel());
        assertEquals("4.0 credits", line.getText());
        assertEquals(NOT_MET, line.getStatus());
        assertEquals(Optional.of("3.5 of 4.0 credits"), line.getReason());
        assertEquals(List.of("PHL281H1"), line.getClaimed());
        assertEquals(List.of(CHILD), line.getChildren());
    }

    @Test
    void reasonIsOptional() {
        assertEquals(Optional.empty(), CHILD.getReason());
    }

    @Test
    void copiesClaimedAndChildren() {
        List<String> claimed = new ArrayList<>(List.of("PHL281H1"));
        List<RowLine> children = new ArrayList<>(List.of(CHILD));
        RowLine line = new RowLine("1", "4.0 credits", MET, null, claimed, children);

        claimed.add("PHL245H1");
        children.add(CHILD);

        assertEquals(List.of("PHL281H1"), line.getClaimed());
        assertEquals(List.of(CHILD), line.getChildren());
        assertThrows(UnsupportedOperationException.class, () -> line.getClaimed().add("PHL245H1"));
        assertThrows(UnsupportedOperationException.class, () -> line.getChildren().add(CHILD));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new RowLine(null, "t", MET, null, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new RowLine("l", null, MET, null, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new RowLine("l", "t", null, null, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new RowLine("l", "t", MET, null, null, List.of()));
        assertThrows(NullPointerException.class, () -> new RowLine("l", "t", MET, null, List.of(), null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(RowLine.class)
                .withPrefabValues(RowLine.class, CHILD, new RowLine("2", "b", NOT_MET, null, List.of(), List.of()))
                .withNonnullFields("label", "text", "status", "claimed", "children")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        RowLine line = new RowLine("Requirement 1", "4.0 credits", NOT_MET, "short", List.of("PHL281H1"),
                List.of());

        assertEquals("RowLine{Requirement 1: NOT_MET \"4.0 credits\", reason=short, claimed=[PHL281H1], "
                + "children=[]}", line.toString());
    }
}
