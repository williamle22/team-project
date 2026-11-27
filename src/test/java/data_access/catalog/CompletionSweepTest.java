package data_access.catalog;

import entity.catalog.Course;
import entity.catalog.Program;
import entity.evaluation.AcademicSnapshot;
import entity.evaluation.CompletionEvaluator;
import entity.evaluation.CourseRecord;
import entity.evaluation.EnrolmentEvaluator;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CompletionSweepTest {
    private static final Path REAL = Paths.get("data", "dgx.db");

    @Test
    void everyProgramEvaluatesWithoutThrowing() {
        assumeTrue(Files.exists(REAL), "no data/dgx.db");

        InMemoryCatalogRepository catalog = new SqliteCatalogLoader().load(REAL);
        List<AcademicSnapshot> snapshots = List.of(AcademicSnapshot.builder().build(), sampleTranscript(catalog));
        EnrolmentEvaluator enrolment = new EnrolmentEvaluator();
        CompletionEvaluator completion = new CompletionEvaluator();

        for (Program p : catalog.allPrograms()) {
            for (AcademicSnapshot snapshot : snapshots) {
                assertDoesNotThrow(() -> {
                    enrolment.evaluate(p.getEnrolment().orElse(null), snapshot);
                    p.getCompletion().ifPresent(c -> completion.evaluate(c, snapshot));
                }, p.getCode());
            }
        }
    }

    private static AcademicSnapshot sampleTranscript(InMemoryCatalogRepository catalog) {
        List<Course> courses = catalog.allCourses().stream().sorted(Comparator.comparing(Course::getCode))
                .collect(Collectors.toList());
        AcademicSnapshot.Builder snapshot = AcademicSnapshot.builder();

        for (int i = 0; i < courses.size(); i += 18) {
            snapshot.taken(new CourseRecord(courses.get(i), 75, false, i % 90 == 0));
        }

        return snapshot.build();
    }
}
