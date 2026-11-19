package use_case.search_courses;

import entity.catalog.Campus;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchResultItemTest {

    private static SearchResultItem item(List<Integer> breadth, boolean offered) {
        return new SearchResultItem("CSC148H1", "Introduction to Computer Science", 0.5, Campus.UTSG, breadth,
                offered);
    }

    @Test
    void gettersReturnConstructorValues() {
        SearchResultItem item = item(List.of(5), true);
        assertEquals("CSC148H1", item.getCode());
        assertEquals("Introduction to Computer Science", item.getTitle());
        assertEquals(0.5, item.getWeight());
        assertEquals(Campus.UTSG, item.getCampus());
        assertEquals(List.of(5), item.getBreadth());
        assertTrue(item.isOfferedThisYear());
    }

    @Test
    void breadthIsCopied() {
        List<Integer> breadth = new ArrayList<>(List.of(4));
        SearchResultItem item = item(breadth, true);
        breadth.add(5);
        assertEquals(List.of(4), item.getBreadth());
        assertThrows(UnsupportedOperationException.class, () -> item.getBreadth().add(1));
    }

    @Test
    void rejectsNullFields() {
        List<Integer> none = List.of();
        assertThrows(NullPointerException.class, () -> new SearchResultItem(null, "t", 0.5, Campus.UTSG, none, true));
        assertThrows(NullPointerException.class, () -> new SearchResultItem("c", null, 0.5, Campus.UTSG, none, true));
        assertThrows(NullPointerException.class, () -> new SearchResultItem("c", "t", 0.5, null, none, true));
        assertThrows(NullPointerException.class, () -> new SearchResultItem("c", "t", 0.5, Campus.UTSG, null, true));
    }

    @Test
    void toStringShowsCodeAndOffering() {
        assertTrue(item(List.of(), true).toString().contains("CSC148H1"));
        assertFalse(item(List.of(), true).toString().contains("not offered"));
        assertTrue(item(List.of(), false).toString().contains("not offered"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchResultItem.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
