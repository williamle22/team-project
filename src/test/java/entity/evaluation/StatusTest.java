package entity.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static entity.evaluation.Status.MET;
import static entity.evaluation.Status.NOT_MET;
import static entity.evaluation.Status.REVIEW;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusTest {

    @Test
    void allOfEachBranch() {
        assertEquals(NOT_MET, Status.allOf(List.of(MET, REVIEW, NOT_MET)));
        assertEquals(REVIEW, Status.allOf(List.of(MET, REVIEW)));
        assertEquals(MET, Status.allOf(List.of(MET, MET)));
    }

    @Test
    void allOfEmptyIsMet() {
        assertEquals(MET, Status.allOf(List.of()));
    }

    @Test
    void anyOfEachBranch() {
        assertEquals(MET, Status.anyOf(List.of(NOT_MET, REVIEW, MET)));
        assertEquals(REVIEW, Status.anyOf(List.of(NOT_MET, REVIEW)));
        assertEquals(NOT_MET, Status.anyOf(List.of(NOT_MET, NOT_MET)));
    }

    @Test
    void anyOfEmptyIsNotMet() {
        assertEquals(NOT_MET, Status.anyOf(List.of()));
    }

    @Test
    void atLeastMet() {
        assertEquals(MET, Status.atLeast(2, List.of(MET, NOT_MET, MET)));
    }

    @Test
    void atLeastReview() {
        assertEquals(REVIEW, Status.atLeast(2, List.of(MET, REVIEW)));
    }

    @Test
    void atLeastNotMet() {
        assertEquals(NOT_MET, Status.atLeast(2, List.of(MET, NOT_MET, NOT_MET)));
        assertEquals(NOT_MET, Status.atLeast(3, List.of(MET, REVIEW)));
    }

    @Test
    void atLeastZeroIsMet() {
        assertEquals(MET, Status.atLeast(0, List.of()));
    }

    @Test
    void negateEach() {
        assertEquals(NOT_MET, MET.negate());
        assertEquals(MET, NOT_MET.negate());
        assertEquals(REVIEW, REVIEW.negate());
    }
}
