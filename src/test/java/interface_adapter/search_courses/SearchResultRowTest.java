package interface_adapter.search_courses;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SearchResultRowTest {

    @Test
    void gettersReturnConstructorValues() {
        SearchResultRow row = new SearchResultRow("CSC148H1", "Introduction to Computer Science", "0.5", "UTSG");
        assertEquals("CSC148H1", row.getCode());
        assertEquals("Introduction to Computer Science", row.getTitle());
        assertEquals("0.5", row.getCredits());
        assertEquals("UTSG", row.getDetails());
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new SearchResultRow(null, "t", "0.5", ""));
        assertThrows(NullPointerException.class, () -> new SearchResultRow("c", null, "0.5", ""));
        assertThrows(NullPointerException.class, () -> new SearchResultRow("c", "t", null, ""));
        assertThrows(NullPointerException.class, () -> new SearchResultRow("c", "t", "0.5", null));
    }

    @Test
    void toStringShowsCode() {
        assertTrue(new SearchResultRow("CSC148H1", "t", "0.5", "UTSG").toString().contains("CSC148H1"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchResultRow.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
