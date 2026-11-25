package use_case.check_graduation;

/** Use Case 10: Check Graduation Eligibility. */
public interface CheckGraduationInputBoundary {
    /** Checks the plan and sends the result to the presenter. */
    void execute(CheckGraduationInputData input);
}
