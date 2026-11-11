package entity.requirement;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseFilterTest {

    @Test
    void defaultsAreEmpty() {
        CourseFilter f = CourseFilter.builder().build();
        assertTrue(f.getCourses().isEmpty());
        assertTrue(f.getDesignators().isEmpty());
        assertTrue(f.getLevels().isEmpty());
        assertTrue(f.getBreadth().isEmpty());
        assertTrue(f.getLists().isEmpty());
        assertTrue(f.getRequirements().isEmpty());
        assertTrue(f.getExclude().isEmpty());
        assertTrue(f.getExcludeDesignators().isEmpty());
        assertTrue(f.getMaxCourseAmount().isEmpty());
        assertTrue(f.getMinGrade().isEmpty());
        assertFalse(f.isTransfer());
        assertFalse(f.isCrNcr());
    }

    @Test
    void builderSetsEveryField() {
        CourseFilter f = CourseFilter.builder()
                .courses(List.of("HPS100H1"))
                .designators(List.of("HPS"))
                .levels(List.of(300, 400))
                .breadth(List.of(4, 5))
                .lists(List.of("Group 2"))
                .requirements(List.of(3))
                .exclude(List.of("HPS390H1"))
                .excludeDesignators(List.of("JHE"))
                .maxCourseAmount(0.5)
                .minGrade(70)
                .transfer(true)
                .crNcr(true)
                .build();
        assertEquals(List.of("HPS100H1"), f.getCourses());
        assertEquals(List.of("HPS"), f.getDesignators());
        assertEquals(List.of(300, 400), f.getLevels());
        assertEquals(List.of(4, 5), f.getBreadth());
        assertEquals(List.of("Group 2"), f.getLists());
        assertEquals(List.of(3), f.getRequirements());
        assertEquals(List.of("HPS390H1"), f.getExclude());
        assertEquals(List.of("JHE"), f.getExcludeDesignators());
        assertEquals(0.5, f.getMaxCourseAmount().orElseThrow());
        assertEquals(70, f.getMinGrade().orElseThrow());
        assertTrue(f.isTransfer());
        assertTrue(f.isCrNcr());
    }

    @Test
    void anyMatchesAnyCourse() {
        assertTrue(CourseFilter.any().matchesAnyCourse());
        assertEquals(CourseFilter.builder().build(), CourseFilter.any());
    }

    @Test
    void designatorsOrCoursesMeanNotAnyCourse() {
        assertFalse(CourseFilter.builder().designators(List.of("HPS")).build().matchesAnyCourse());
        assertFalse(CourseFilter.builder().courses(List.of("HPS100H1")).build().matchesAnyCourse());
        assertTrue(CourseFilter.builder().levels(List.of(300)).build().matchesAnyCourse());
    }

    @Test
    void builderCopiesLists() {
        List<String> designators = new ArrayList<>(List.of("HPS"));
        CourseFilter f = CourseFilter.builder().designators(designators).build();
        designators.add("PHL");
        assertEquals(List.of("HPS"), f.getDesignators());
    }

    @Test
    void getterListsAreUnmodifiable() {
        CourseFilter f = CourseFilter.builder().designators(List.of("HPS")).build();
        assertThrows(UnsupportedOperationException.class, () -> f.getDesignators().add("PHL"));
    }

    @Test
    void rejectsNullList() {
        assertThrows(NullPointerException.class, () -> CourseFilter.builder().courses(null));
    }

    @Test
    void rejectsBadMinGrade() {
        assertThrows(IllegalArgumentException.class, () -> CourseFilter.builder().minGrade(101).build());
        assertThrows(IllegalArgumentException.class, () -> CourseFilter.builder().minGrade(-1).build());
    }

    @Test
    void rejectsNegativeMaxCourseAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> CourseFilter.builder().maxCourseAmount(-0.5).build());
    }

    @Test
    void equalFiltersAreEqual() {
        CourseFilter a = CourseFilter.builder().designators(List.of("HPS")).levels(List.of(300, 400)).build();
        CourseFilter b = CourseFilter.builder().designators(List.of("HPS")).levels(List.of(300, 400)).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentFiltersAreNotEqual() {
        CourseFilter a = CourseFilter.builder().designators(List.of("HPS")).build();
        assertNotEquals(a, CourseFilter.builder().designators(List.of("PHL")).build());
        assertNotEquals(a, CourseFilter.builder().designators(List.of("HPS")).minGrade(70).build());
        assertNotEquals(a, CourseFilter.builder().designators(List.of("HPS")).transfer(true).build());
    }
}