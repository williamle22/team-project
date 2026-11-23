package interface_adapter.search_programs;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchProgramsStateTest {
    private static final ProgramResultRow ROW = new ProgramResultRow("ASMAJ1689", "t", "Major");

    @Test
    void defaultsToEmpty() {
        SearchProgramsState s = new SearchProgramsState();
        assertEquals(List.of(), s.getResults());
        assertEquals("", s.getMessage());
    }

    @Test
    void settersUpdateFields() {
        SearchProgramsState s = new SearchProgramsState();
        s.setResults(List.of(ROW));
        s.setMessage("Program data is still loading.");
        assertEquals(List.of(ROW), s.getResults());
        assertEquals("Program data is still loading.", s.getMessage());
    }

    @Test
    void resultsAreCopied() {
        List<ProgramResultRow> rows = new ArrayList<>(List.of(ROW));
        SearchProgramsState s = new SearchProgramsState();
        s.setResults(rows);
        rows.clear();
        assertEquals(List.of(ROW), s.getResults());
        assertThrows(UnsupportedOperationException.class, () -> s.getResults().add(ROW));
    }

    @Test
    void toStringShowsCountAndMessage() {
        SearchProgramsState s = new SearchProgramsState();
        s.setResults(List.of(ROW));
        s.setMessage("No programs match.");
        assertTrue(s.toString().contains("1 results"));
        assertTrue(s.toString().contains("No programs match."));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchProgramsState.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }
}
