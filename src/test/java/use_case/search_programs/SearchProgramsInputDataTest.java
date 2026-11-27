package use_case.search_programs;

import entity.catalog.ProgramType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static entity.catalog.ProgramType.MAJOR;
import static entity.catalog.ProgramType.MINOR;
import static org.junit.jupiter.api.Assertions.*;

class SearchProgramsInputDataTest {
    @Test
    void gettersReturnConstructorValues() {
        SearchProgramsInputData input = new SearchProgramsInputData(" computer science ", Set.of(MAJOR));
        assertEquals(" computer science ", input.getQuery());
        assertEquals(Set.of(MAJOR), input.getTypes());
    }

    @Test
    void typesAreCopied() {
        Set<ProgramType> types = new HashSet<>(Set.of(MAJOR));
        SearchProgramsInputData input = new SearchProgramsInputData("csc", types);
        types.add(MINOR);
        assertEquals(Set.of(MAJOR), input.getTypes());
        assertThrows(UnsupportedOperationException.class, () -> input.getTypes().add(MINOR));
    }

    @Test
    void rejectsNullFields() {
        assertThrows(NullPointerException.class, () -> new SearchProgramsInputData(null, Set.of()));
        assertThrows(NullPointerException.class, () -> new SearchProgramsInputData("csc", null));
    }

    @Test
    void toStringShowsQuery() {
        assertTrue(new SearchProgramsInputData("computer science", Set.of()).toString().contains("computer science"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(SearchProgramsInputData.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
