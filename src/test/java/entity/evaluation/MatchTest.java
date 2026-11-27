package entity.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.evaluation.Match.MAYBE;
import static entity.evaluation.Match.NO;
import static entity.evaluation.Match.YES;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MatchTest {

    @Test
    void allOfEachBranch() {
        assertEquals(NO, Match.allOf(List.of(YES, MAYBE, NO)));
        assertEquals(MAYBE, Match.allOf(List.of(YES, MAYBE)));
        assertEquals(YES, Match.allOf(List.of(YES, YES)));
    }

    @Test
    void allOfEmptyIsYes() {
        assertEquals(YES, Match.allOf(List.of()));
    }

    @Test
    void anyOfEachBranch() {
        assertEquals(YES, Match.anyOf(List.of(NO, MAYBE, YES)));
        assertEquals(MAYBE, Match.anyOf(List.of(NO, MAYBE)));
        assertEquals(NO, Match.anyOf(List.of(NO, NO)));
    }

    @Test
    void anyOfEmptyIsNo() {
        assertEquals(NO, Match.anyOf(List.of()));
    }
}
