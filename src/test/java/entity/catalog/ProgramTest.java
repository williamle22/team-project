package entity.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramTest {

    @Test
    void equalityIsByCode() {
        Program a = new Program("ASMIN1001", ProgramType.MINOR, "1001", "History and Philosophy of Science",
                null, null, List.of());
        Program b = new Program("ASMIN1001", ProgramType.MINOR, "1001", "A different title", null, null, List.of());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Program("ASMAJ1001", ProgramType.MAJOR, "1001", "History and Philosophy of Science",
                null, null, List.of()));
    }
}
