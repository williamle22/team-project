package use_case.add_course_to_plan;

import entity.term.AcademicTerm;

import java.util.UUID;

/** Adds a catalog course to a plan at the requested Fall or Winter term. */
public interface AddCourseToPlanInputBoundary {
    void execute(UUID planId, String courseCode, AcademicTerm targetTerm);
}
