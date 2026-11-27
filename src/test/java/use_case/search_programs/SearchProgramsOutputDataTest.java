package use_case.search_programs;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static org.junit.jupiter.api.Assertions.*;

class SearchProgramsOutputDataTest {
    private static final ProgramResultItem CS_MAJOR =
            new ProgramResultItem("ASMAJ1689", "Computer Science Major (Science Program)", MAJOR);
    private static final ProgramResultItem CS_MINOR =
            new ProgramResultItem("ASMIN1689", "Computer Science Minor (Science Program)", MINOR);

    @Test
    void resultsAreCopied() {
        List<ProgramResultItem> results = new ArrayList<>(List.of(CS_MAJOR));
        SearchProgramsOutputData data = new SearchProgramsOutputData(results);
        results.add(CS_MINOR);
        assertEquals(List.of(CS_MAJOR), data.getResults());
        assertThrows(UnsupportedOperationException.class, () -> data.getResults().add(CS_MINOR));
    }

    @Test
    void toStringShowsCount() {
        assertTrue(new SearchProgramsOutputData(List.of(CS_MAJOR, CS_MINOR)).toString().contains("2 programs"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchProgramsOutputData.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
