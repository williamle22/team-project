package entity.catalog;

import entity.requirement.CourseReq;
import entity.requirement.ProfileReq;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompletionRowTest {

    private static final CourseReq CSC148 = new CourseReq("CSC148H1", null, false);

    private static CompletionRow row(int number, Integer parent, boolean transcriptScope) {
        return new CompletionRow(number, RowKind.ALLOCATION, null, null, parent, List.of(), CSC148, null,
                transcriptScope, List.of(), "");
    }

    @Test
    void topLevelHasNoParent() {
        assertTrue(row(1, null, false).isTopLevel());
        assertTrue(row(1, null, false).getParent().isEmpty());
        assertFalse(row(2, 1, false).isTopLevel());
        assertEquals(1, row(2, 1, false).getParent().orElseThrow());
    }

    @Test
    void optionGroupHasNoReq() {
        CompletionRow group = new CompletionRow(1, RowKind.CHOICE, "Option A", null, null, List.of(), null, null,
                false, List.of(), "");
        assertTrue(group.isOptionGroup());
        assertTrue(group.getReq().isEmpty());
        assertFalse(row(2, null, false).isOptionGroup());
    }

    @Test
    void transcriptScope() {
        assertTrue(row(1, null, true).isTranscriptScope());
        assertFalse(row(1, null, false).isTranscriptScope());
    }

    @Test
    void audienceIsOptional() {
        ProfileReq audience = new ProfileReq("admission_category", "Computer Science", "Students admitted to CS", false);
        CompletionRow r = new CompletionRow(1, RowKind.ALLOCATION, null, null, null, List.of(), CSC148, audience,
                false, List.of(), "");
        assertEquals(audience, r.getAudience().orElseThrow());
        assertTrue(row(1, null, false).getAudience().isEmpty());
    }

    @Test
    void rejectsZeroNumber() {
        assertThrows(IllegalArgumentException.class, () -> row(0, null, false));
    }
}
