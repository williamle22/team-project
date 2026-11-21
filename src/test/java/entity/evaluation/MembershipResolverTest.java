package entity.evaluation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutil.CatalogFixtures.record;

class MembershipResolverTest {

    @Test
    void noneCantTellListsOrRows() {
        CourseRecord r = record("CSC148H1");

        assertEquals(Match.MAYBE, MembershipResolver.NONE.onList("Group 1", r));
        assertEquals(Match.MAYBE, MembershipResolver.NONE.inRowPool(1, r));
        assertTrue(MembershipResolver.NONE.rowStatus(1).isEmpty());
    }
}
