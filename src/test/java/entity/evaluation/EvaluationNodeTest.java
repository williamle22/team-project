package entity.evaluation;

import entity.requirement.AndReq;
import entity.requirement.CourseReq;
import entity.requirement.Requirement;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationNodeTest {

    private static final CourseReq CSC148 = new CourseReq("CSC148H1", null, false);
    private static final CourseReq MAT137 = new CourseReq("MAT137Y1", null, false);

    @Test
    void allCountedWalksSubtree() {
        EvaluationNode csc = EvaluationNode.leaf(CSC148, Status.MET, null, List.of("CSC148H1"));
        EvaluationNode mat = EvaluationNode.leaf(MAT137, Status.MET, null, List.of("MAT137Y1"));
        EvaluationNode inner = new EvaluationNode(null, Status.MET, null, List.of(mat), List.of("CSC108H1"));
        EvaluationNode root = new EvaluationNode(null, Status.MET, null, List.of(csc, inner), List.of());

        assertEquals(List.of("CSC148H1", "CSC108H1", "MAT137Y1"), new ArrayList<>(root.allCounted()));
    }

    @Test
    void allCountedKeepsFirstSeenOrderAndDropsDuplicates() {
        EvaluationNode a = EvaluationNode.leaf(CSC148, Status.MET, null, List.of("CSC148H1", "MAT137Y1"));
        EvaluationNode b = EvaluationNode.leaf(MAT137, Status.MET, null, List.of("MAT137Y1", "CSC148H1"));
        EvaluationNode root = new EvaluationNode(null, Status.MET, null, List.of(a, b), List.of("STA130H1"));

        assertEquals(List.of("STA130H1", "CSC148H1", "MAT137Y1"), new ArrayList<>(root.allCounted()));
    }

    @Test
    void allCountedOfPlainLeafIsEmpty() {
        assertTrue(EvaluationNode.leaf(CSC148, Status.NOT_MET, "not taken").allCounted().isEmpty());
    }

    @Test
    void noneIsMet() {
        EvaluationNode none = EvaluationNode.none("no prerequisites");

        assertEquals(Status.MET, none.getStatus());
        assertTrue(none.getRequirement().isEmpty());
        assertEquals("no prerequisites", none.getReason().orElseThrow());
        assertTrue(none.getChildren().isEmpty());
        assertTrue(none.getCounted().isEmpty());
    }

    @Test
    void leafHasNoChildrenOrCounted() {
        EvaluationNode leaf = EvaluationNode.leaf(CSC148, Status.NOT_MET, "not taken");

        assertEquals(CSC148, leaf.getRequirement().orElseThrow());
        assertEquals(Status.NOT_MET, leaf.getStatus());
        assertEquals("not taken", leaf.getReason().orElseThrow());
        assertTrue(leaf.getChildren().isEmpty());
        assertTrue(leaf.getCounted().isEmpty());
    }

    @Test
    void leafWithCounted() {
        EvaluationNode leaf = EvaluationNode.leaf(CSC148, Status.MET, null, List.of("CSC148H1"));

        assertEquals(List.of("CSC148H1"), leaf.getCounted());
        assertTrue(leaf.getReason().isEmpty());
    }

    @Test
    void constructorKeepsChildren() {
        Requirement and = new AndReq(List.of(CSC148, MAT137), null, false);
        EvaluationNode csc = EvaluationNode.leaf(CSC148, Status.MET, null);
        EvaluationNode mat = EvaluationNode.leaf(MAT137, Status.REVIEW, "grade unknown");
        EvaluationNode node = new EvaluationNode(and, Status.REVIEW, null, List.of(csc, mat), List.of());

        assertEquals(and, node.getRequirement().orElseThrow());
        assertEquals(List.of(csc, mat), node.getChildren());
    }

    @Test
    void constructorCopiesLists() {
        List<EvaluationNode> children = new ArrayList<>();
        List<String> counted = new ArrayList<>(List.of("CSC148H1"));
        EvaluationNode node = new EvaluationNode(null, Status.MET, null, children, counted);

        children.add(EvaluationNode.none(null));
        counted.add("MAT137Y1");

        assertTrue(node.getChildren().isEmpty());
        assertEquals(List.of("CSC148H1"), node.getCounted());
        assertThrows(UnsupportedOperationException.class, () -> node.getCounted().add("STA130H1"));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new EvaluationNode(null, null, null, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new EvaluationNode(null, Status.MET, null, null, List.of()));
        assertThrows(NullPointerException.class, () -> new EvaluationNode(null, Status.MET, null, List.of(), null));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(EvaluationNode.class)
                .withPrefabValues(EvaluationNode.class, EvaluationNode.none("a"), EvaluationNode.none("b"))
                .withPrefabValues(Requirement.class, CSC148, MAT137)
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }

    @Test
    void toStringShowsStatusAndReason() {
        String s = EvaluationNode.leaf(CSC148, Status.NOT_MET, "not taken").toString();

        assertTrue(s.contains("NOT_MET"));
        assertTrue(s.contains("not taken"));
    }
}
