package entity.catalog;

import entity.requirement.TextKind;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExclusionItemTest {

    private static ExclusionItem courses(String... codes) {
        return new ExclusionItem(List.of(codes), List.of(), List.of(), List.of(), false, null, null, null);
    }

    @Test
    void courseSetItem() {
        ExclusionItem item = courses("JCC250H1");
        assertTrue(item.isCourseSet());
        assertFalse(item.isFilter());
        assertFalse(item.isText());
        assertFalse(item.hasNote());
    }

    @Test
    void filterItem() {
        ExclusionItem item = new ExclusionItem(List.of(), List.of("FSL"), List.of(400), List.of(),
                false, null, null, null);
        assertTrue(item.isFilter());
        assertFalse(item.isCourseSet());
        assertFalse(item.isText());
    }

    @Test
    void filterWithNote() {
        ExclusionItem item = new ExclusionItem(List.of(), List.of("CSC"), List.of(),
                List.of("CSC196H1"), false, "except AP, IB, CAPE or GCE Transfer Credits", null, null);
        assertTrue(item.isFilter());
        assertTrue(item.hasNote());
    }

    @Test
    void textItem() {
        ExclusionItem item = new ExclusionItem(List.of(), List.of(), List.of(), List.of(),
                false, null, TextKind.PREPARATION, "OAC Hebrew");
        assertTrue(item.isText());
        assertFalse(item.isCourseSet());
        assertFalse(item.isFilter());
        assertEquals(TextKind.PREPARATION, item.getKind().orElseThrow());
    }

    @Test
    void listsAreNeverNull() {
        assertThrows(NullPointerException.class,
                () -> new ExclusionItem(null, List.of(), List.of(), List.of(), false, null, null, null));
    }

    @Test
    void equalByValue() {
        assertEquals(courses("JCC250H1"), courses("JCC250H1"));
    }

    @Test
    void gettersReturnConstructorValues() {
        ExclusionItem item = new ExclusionItem(List.of("CSC148H1"), List.of("CSC"), List.of(100),
                List.of("CSC196H1"), true, "taken previously or concurrently", TextKind.OTHER, "a CSC course");
        assertEquals(List.of("CSC148H1"), item.getCourses());
        assertEquals(List.of("CSC"), item.getDesignators());
        assertEquals(List.of(100), item.getLevels());
        assertEquals(List.of("CSC196H1"), item.getExclude());
        assertTrue(item.isConcurrent());
        assertEquals("taken previously or concurrently", item.getNote().orElseThrow());
        assertEquals(TextKind.OTHER, item.getKind().orElseThrow());
        assertEquals("a CSC course", item.getText().orElseThrow());
    }

    @Test
    void optionalPartsAreEmptyWhenAbsent() {
        ExclusionItem item = courses("CSC148H1");
        assertTrue(item.getNote().isEmpty());
        assertTrue(item.getKind().isEmpty());
        assertTrue(item.getText().isEmpty());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(ExclusionItem.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringIncludesCourses() {
        assertTrue(courses("JCC250H1").toString().contains("JCC250H1"));
    }
}
