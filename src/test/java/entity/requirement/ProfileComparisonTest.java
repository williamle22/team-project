package entity.requirement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileComparisonTest {

    @Test
    void hasPrefix() {
        assertEquals(ProfileComparison.HAS, ProfileComparison.forAttribute("has_transfer_credits"));
    }

    @Test
    void minPrefix() {
        assertEquals(ProfileComparison.MIN, ProfileComparison.forAttribute("min_year_of_study"));
    }

    @Test
    void maxPrefix() {
        assertEquals(ProfileComparison.MAX, ProfileComparison.forAttribute("max_cr_ncr"));
    }

    @Test
    void bySuffix() {
        assertEquals(ProfileComparison.BY, ProfileComparison.forAttribute("complete_by"));
    }

    @Test
    void anythingElseIsEquals() {
        assertEquals(ProfileComparison.EQUALS, ProfileComparison.forAttribute("faculty"));
    }

    @Test
    void prefixWinsOverSuffix() {
        // checked in order: has_, min_, max_, then _by
        assertEquals(ProfileComparison.MIN, ProfileComparison.forAttribute("min_credits_by"));
    }

    @Test
    void prefixMustBeAtTheStart() {
        assertEquals(ProfileComparison.EQUALS, ProfileComparison.forAttribute("program_min_gpa"));
    }
}