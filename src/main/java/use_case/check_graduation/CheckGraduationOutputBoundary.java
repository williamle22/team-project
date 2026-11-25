package use_case.check_graduation;

public interface CheckGraduationOutputBoundary {
    void prepareSuccessView(CheckGraduationOutputData data);
    void prepareFailView(String message);
}
