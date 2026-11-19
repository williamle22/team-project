package entity.evaluation;

import entity.catalog.Course;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.course;
import static testutil.CatalogFixtures.record;

class CourseRecordTest {

    @Test
    void gettersReturnConstructorValues() {
        Course csc148 = course("CSC148H1");
        CourseRecord r = new CourseRecord(csc148, 75, true, true);

        assertEquals(csc148, r.getCourse());
        assertEquals("CSC148H1", r.getCode());
        assertEquals(75, r.getGrade().orElseThrow());
        assertTrue(r.isTransfer());
        assertTrue(r.isCrNcr());
    }

    @Test
    void unknownGradeIsEmpty() {
        CourseRecord r = record("CSC148H1");

        assertTrue(r.getGrade().isEmpty());
        assertFalse(r.isTransfer());
        assertFalse(r.isCrNcr());
    }

    @Test
    void rejectsNullCourse() {
        assertThrows(NullPointerException.class, () -> new CourseRecord(null, 75, false, false));
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(CourseRecord.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringShowsCodeGradeAndFlags() {
        assertEquals("CourseRecord{CSC148H1, grade=75}", record("CSC148H1", 75).toString());
        assertEquals("CourseRecord{CSC148H1, grade=null, transfer, CR/NCR}",
                new CourseRecord(course("CSC148H1"), null, true, true).toString());
    }
}
