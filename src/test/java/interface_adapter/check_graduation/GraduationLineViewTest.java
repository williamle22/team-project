package interface_adapter.check_graduation;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraduationLineViewTest {
    private static final GraduationLineView CHILD =
            new GraduationLineView("Requirement 2", "PHL281H1", "met", "", "PHL281H1", List.of());

    @Test
    void gettersReturnConstructorValues() {
        GraduationLineView v = new GraduationLineView("Requirement 1", "4.0 credits", "not met",
                "3.5 of 4.0 credits", "PHL281H1, PHL245H1", List.of(CHILD));

        assertEquals("Requirement 1", v.getLabel());
        assertEquals("4.0 credits", v.getText());
        assertEquals("not met", v.getStatus());
        assertEquals("3.5 of 4.0 credits", v.getReason());
        assertEquals("PHL281H1, PHL245H1", v.getClaimed());
        assertEquals(List.of(CHILD), v.getChildren());
    }

    @Test
    void copiesChildren() {
        List<GraduationLineView> children = new ArrayList<>(List.of(CHILD));
        GraduationLineView v = new GraduationLineView("l", "t", "met", "", "", children);

        children.add(CHILD);

        assertEquals(List.of(CHILD), v.getChildren());
        assertThrows(UnsupportedOperationException.class, () -> v.getChildren().add(CHILD));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new GraduationLineView(null, "t", "met", "", "", List.of()));
        assertThrows(NullPointerException.class, () -> new GraduationLineView("l", null, "met", "", "", List.of()));
        assertThrows(NullPointerException.class, () -> new GraduationLineView("l", "t", null, "", "", List.of()));
        assertThrows(NullPointerException.class, () -> new GraduationLineView("l", "t", "met", null, "", List.of()));
        assertThrows(NullPointerException.class, () -> new GraduationLineView("l", "t", "met", "", null, List.of()));
        assertThrows(NullPointerException.class, () -> new GraduationLineView("l", "t", "met", "", "", null));
    }

    @Test
    void equalsAndHashCode() {
        EqualsVerifier.forClass(GraduationLineView.class)
                .withPrefabValues(GraduationLineView.class, CHILD,
                        new GraduationLineView("b", "b", "review", "", "", List.of()))
                .withNonnullFields("label", "text", "status", "reason", "claimed", "children")
                .verify();
    }

    @Test
    void toStringShowsFields() {
        GraduationLineView v = new GraduationLineView("Requirement 1", "4.0 credits", "not met", "short",
                "PHL281H1", List.of());

        assertEquals("GraduationLineView{Requirement 1: not met \"4.0 credits\", reason=short, claimed=PHL281H1, "
                + "children=[]}", v.toString());
    }
}
