package interface_adapter.dgx;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AcademicPlannerStateTest {
    @Test
    void defaultsToNoTermsAndNoMessage() {
        AcademicPlannerState state = new AcademicPlannerState();

        assertEquals(List.of(), state.getTerms());
        assertEquals("", state.getMessage());
    }

    @Test
    void copiesTermListAndCourseRows() {
        AcademicPlannerState.CourseRow course = new AcademicPlannerState.CourseRow("CSC148H1");
        List<AcademicPlannerState.CourseRow> courses = new ArrayList<>(List.of(course));
        AcademicPlannerState.TermRow term = new AcademicPlannerState.TermRow(2027, "Summer", courses);
        List<AcademicPlannerState.TermRow> terms = new ArrayList<>(List.of(term));
        AcademicPlannerState state = new AcademicPlannerState(terms, "Added course.");
        courses.clear();
        terms.clear();

        assertEquals(List.of(term), state.getTerms());
        assertEquals(List.of(course), state.getTerms().get(0).getCourses());
        assertThrows(UnsupportedOperationException.class, () -> state.getTerms().clear());
        assertThrows(UnsupportedOperationException.class, () -> state.getTerms().get(0).getCourses().clear());
        assertEquals("Added course.", state.getMessage());
    }

    @Test
    void termRowBuildsLabelAndCourseRowExposesCode() {
        AcademicPlannerState.TermRow term = new AcademicPlannerState.TermRow(2027, "Summer",
                List.of(new AcademicPlannerState.CourseRow("CSC148H1")));

        assertEquals("Summer 2027", term.getLabel());
        assertEquals("CSC148H1", term.getCourses().get(0).getCode());
    }

    @Test
    void rejectsNullRequiredValues() {
        assertThrows(NullPointerException.class, () -> new AcademicPlannerState(null, ""));
        assertThrows(NullPointerException.class, () -> new AcademicPlannerState(List.of(), null));
        assertThrows(NullPointerException.class, () -> new AcademicPlannerState.TermRow(2027, null, List.of()));
        assertThrows(NullPointerException.class, () -> new AcademicPlannerState.TermRow(2027, "Summer", null));
        assertThrows(NullPointerException.class, () -> new AcademicPlannerState.CourseRow(null));
    }
}
