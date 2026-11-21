package entity.evaluation;

import entity.term.AcademicTerm;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static testutil.CatalogFixtures.record;

class AcademicSnapshotTest {

    @Test
    void emptyBuilderGivesEmptySnapshot() {
        AcademicSnapshot s = AcademicSnapshot.builder().build();

        assertTrue(s.getTaken().isEmpty());
        assertTrue(s.getConcurrent().isEmpty());
        assertTrue(s.getEnrolledPrograms().isEmpty());
        assertTrue(s.getIntendedPrograms().isEmpty());
        assertTrue(s.getCgpa().isEmpty());
        assertTrue(s.getAsOf().isEmpty());
        assertTrue(s.allRecords().isEmpty());
    }

    @Test
    void builderSetsEveryPart() {
        AcademicSnapshot s = AcademicSnapshot.builder()
                .taken(record("CSC148H1", 75))
                .concurrent(record("CSC207H1"))
                .enrolledProgram("ASMAJ1689")
                .intendedProgram("ASSPE1689")
                .cgpa(3.4)
                .profileFact("min_year_of_study", 2)
                .asOf(AcademicTerm.fall(2026))
                .build();

        assertEquals(record("CSC148H1", 75), s.getTaken().get("CSC148H1"));
        assertEquals(record("CSC207H1"), s.getConcurrent().get("CSC207H1"));
        assertEquals(Set.of("ASMAJ1689"), s.getEnrolledPrograms());
        assertEquals(Set.of("ASSPE1689"), s.getIntendedPrograms());
        assertEquals(3.4, s.getCgpa().orElseThrow());
        assertEquals(2, s.profileFact("min_year_of_study").orElseThrow());
        assertEquals(AcademicTerm.fall(2026), s.getAsOf().orElseThrow());
    }

    @Test
    void collectionOverloads() {
        AcademicSnapshot s = AcademicSnapshot.builder()
                .taken(List.of(record("CSC148H1"), record("MAT137Y1")))
                .concurrent(List.of(record("CSC207H1")))
                .build();

        assertEquals(List.of("CSC148H1", "MAT137Y1"), new ArrayList<>(s.getTaken().keySet()));
        assertEquals(List.of("CSC207H1"), new ArrayList<>(s.getConcurrent().keySet()));
    }

    @Test
    void takenTwiceKeepsLastRecord() {
        AcademicSnapshot s = AcademicSnapshot.builder()
                .taken(record("CSC148H1", 45))
                .taken(record("CSC148H1", 80))
                .build();

        assertEquals(80, s.getTaken().get("CSC148H1").getGrade().orElseThrow());
    }

    @Test
    void codeInBothMapsThrows() {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder()
                .taken(record("MAT137Y1"), record("CSC148H1"))
                .concurrent(record("CSC148H1"));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, b::build);
        assertTrue(e.getMessage().contains("CSC148H1"));
    }

    @Test
    void missingProfileFactIsEmpty() {
        AcademicSnapshot s = AcademicSnapshot.builder().profileFact("min_year_of_study", 2).build();

        assertTrue(s.profileFact("admission_category").isEmpty());
    }

    @Test
    void nullCgpaIsUnknown() {
        assertTrue(AcademicSnapshot.builder().cgpa(3.0).cgpa(null).build().getCgpa().isEmpty());
    }

    @Test
    void allRecordsListsTakenThenConcurrent() {
        AcademicSnapshot s = AcademicSnapshot.builder()
                .concurrent(record("CSC207H1"))
                .taken(record("MAT137Y1"), record("CSC148H1"))
                .build();

        List<String> codes = new ArrayList<>();
        for (CourseRecord r : s.allRecords()) {
            codes.add(r.getCode());
        }

        assertEquals(List.of("MAT137Y1", "CSC148H1", "CSC207H1"), codes);
    }

    @Test
    void restrictedToKeepsOnlyThoseCodes() {
        AcademicSnapshot s = AcademicSnapshot.builder()
                .taken(record("CSC148H1"), record("MAT137Y1"), record("STA130H1"))
                .concurrent(record("CSC207H1"), record("CSC236H1"))
                .enrolledProgram("ASMAJ1689")
                .cgpa(3.4)
                .profileFact("min_year_of_study", 2)
                .asOf(AcademicTerm.fall(2026))
                .build();

        AcademicSnapshot r = s.restrictedTo(Set.of("STA130H1", "CSC148H1", "CSC207H1"));

        assertEquals(List.of("CSC148H1", "STA130H1"), new ArrayList<>(r.getTaken().keySet()));
        assertEquals(List.of("CSC207H1"), new ArrayList<>(r.getConcurrent().keySet()));
        assertEquals(s.getEnrolledPrograms(), r.getEnrolledPrograms());
        assertEquals(s.getCgpa(), r.getCgpa());
        assertEquals(2, r.profileFact("min_year_of_study").orElseThrow());
        assertEquals(s.getAsOf(), r.getAsOf());
        assertThrows(UnsupportedOperationException.class, () -> r.getTaken().clear());
        assertEquals(3, s.getTaken().size());
    }

    @Test
    void snapshotIsImmutable() {
        AcademicSnapshot s = AcademicSnapshot.builder().taken(record("CSC148H1")).enrolledProgram("ASMAJ1689").build();

        assertThrows(UnsupportedOperationException.class, () -> s.getTaken().put("MAT137Y1", record("MAT137Y1")));
        assertThrows(UnsupportedOperationException.class, () -> s.getConcurrent().clear());
        assertThrows(UnsupportedOperationException.class, () -> s.getEnrolledPrograms().add("ASMIN1689"));
        assertThrows(UnsupportedOperationException.class, () -> s.getIntendedPrograms().add("ASMIN1689"));
    }

    @Test
    void builderChangesAfterBuildDontLeak() {
        AcademicSnapshot.Builder b = AcademicSnapshot.builder().taken(record("CSC148H1"));
        AcademicSnapshot s = b.build();

        b.taken(record("MAT137Y1")).enrolledProgram("ASMAJ1689").profileFact("min_year_of_study", 2);

        assertEquals(Set.of("CSC148H1"), s.getTaken().keySet());
        assertTrue(s.getEnrolledPrograms().isEmpty());
        assertTrue(s.profileFact("min_year_of_study").isEmpty());
    }

    @Test
    void equalsContract() {
        EqualsVerifier.forClass(AcademicSnapshot.class).suppress(Warning.NULL_FIELDS).verify();
    }

    @Test
    void toStringShowsRecordsAndPrograms() {
        String s = AcademicSnapshot.builder().taken(record("CSC148H1", 75)).enrolledProgram("ASMAJ1689").build()
                .toString();

        assertTrue(s.contains("CSC148H1"));
        assertTrue(s.contains("ASMAJ1689"));
    }
}
