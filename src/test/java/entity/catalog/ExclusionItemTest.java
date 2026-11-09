package entity.catalog;

import entity.requirement.TextKind;
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
}