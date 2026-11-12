package entity.timetable;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TimetableChoiceTest {

    @Test
    void withAddsAndReplaces() {
        TimetableChoice c = TimetableChoice.empty()
                .with(ActivityType.LEC, "LEC0101")
                .with(ActivityType.TUT, "TUT0201")
                .with(ActivityType.LEC, "LEC0102");
        assertEquals(Map.of(ActivityType.LEC, "LEC0102", ActivityType.TUT, "TUT0201"), c.asMap());
    }

    @Test
    void withoutRemoves() {
        TimetableChoice c = TimetableChoice.empty()
                .with(ActivityType.LEC, "LEC0101")
                .with(ActivityType.TUT, "TUT0201")
                .without(ActivityType.TUT);
        assertEquals(Set.of(ActivityType.LEC), c.chosenTypes());
        assertTrue(c.get(ActivityType.TUT).isEmpty());
    }

    @Test
    void emptyHasNoTypes() {
        assertTrue(TimetableChoice.empty().chosenTypes().isEmpty());
        assertTrue(TimetableChoice.empty().get(ActivityType.LEC).isEmpty());
    }

    @Test
    void withDoesNotMutate() {
        TimetableChoice original = TimetableChoice.empty().with(ActivityType.LEC, "LEC0101");
        original.with(ActivityType.LEC, "LEC0102");
        assertEquals("LEC0101", original.get(ActivityType.LEC).get());
    }

    @Test
    void toStringIncludesChosenCodes() {
        assertTrue(TimetableChoice.empty().with(ActivityType.LEC, "LEC0101").toString().contains("LEC0101"));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(TimetableChoice.class).suppress(Warning.NULL_FIELDS).verify();
    }
}
