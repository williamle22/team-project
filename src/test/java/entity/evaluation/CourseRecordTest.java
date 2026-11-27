package entity.evaluation;

import entity.catalog.Course;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        assertEquals("CourseRecord{CSC148H1, grade=75, weight=0.5}", record("CSC148H1", 75).toString());
        assertEquals("CourseRecord{CSC148H1, grade=null, weight=0.5, transfer, CR/NCR}",
                new CourseRecord(course("CSC148H1"), null, true, true).toString());
    }

    @Test
    void unspecifiedTransferTakesDesignatorAndLevelFromCode() {
        CourseRecord r = CourseRecord.unspecifiedTransfer("MAT2**", 2.0).orElseThrow();

        assertEquals("MAT2**", r.getCode());
        assertEquals("MAT", r.getCourse().getDesignator());
        assertEquals(200, r.getCourse().getLevel());
        assertEquals(2.0, r.getWeight());
        assertTrue(r.isTransfer());
        assertTrue(r.isUnspecified());
        assertTrue(r.getGrade().isEmpty());
    }

    @Test
    void unspecifiedTransferRejectsOtherCodes() {
        for (String code : List.of("USP***", "MAT***", "MAT135H1")) {
            assertTrue(CourseRecord.unspecifiedTransfer(code, 0.5).isEmpty());
        }
    }

    @Test
    void couldBeMatchesDesignatorAndLevel() {
        CourseRecord r = CourseRecord.unspecifiedTransfer("MAT1**", 0.5).orElseThrow();

        assertTrue(r.couldBe("MAT135H1"));
        assertFalse(r.couldBe("MAT235H1"));
        assertFalse(record("MAT135H1").couldBe("MAT135H1"));
    }

    @Test
    void weightDefaultsToCourseWeight() {
        assertEquals(1.0, record("MAT137Y1").getWeight());
        assertFalse(record("MAT137Y1").isUnspecified());
    }
}
