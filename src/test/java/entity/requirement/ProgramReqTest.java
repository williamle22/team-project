package entity.requirement;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramReqTest {

    private static final List<String> DRAMA = List.of("ASMAJ1645", "ASSPE1645", "ASMIN1645");

    @Test
    void gettersReturnConstructorValues() {
        ProgramReq r = new ProgramReq(DRAMA, "Drama Major, Specialist or Minor", true);
        assertEquals(DRAMA, r.getPrograms());
        assertEquals("Drama Major, Specialist or Minor", r.getText());
        assertTrue(r.isConcurrent());
    }

    @Test
    void copiesPrograms() {
        List<String> programs = new ArrayList<>(List.of("ASMAJ1645"));
        ProgramReq r = new ProgramReq(programs, "", false);
        programs.add("ASMIN1645");
        assertEquals(List.of("ASMAJ1645"), r.getPrograms());
    }

    @Test
    void rejectsEmptyPrograms() {
        assertThrows(IllegalArgumentException.class, () -> new ProgramReq(List.of(), "", false));
    }

    @Test
    void rejectsNullText() {
        assertThrows(NullPointerException.class, () -> new ProgramReq(DRAMA, null, false));
    }

    @Test
    void equalsComparesConcurrent() {
        assertEquals(new ProgramReq(DRAMA, "Drama", false), new ProgramReq(DRAMA, "Drama", false));
        assertEquals(new ProgramReq(DRAMA, "Drama", false).hashCode(), new ProgramReq(DRAMA, "Drama", false).hashCode());
        assertNotEquals(new ProgramReq(DRAMA, "Drama", false), new ProgramReq(DRAMA, "Drama", true));
    }
}
