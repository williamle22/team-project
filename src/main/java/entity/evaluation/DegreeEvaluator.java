package entity.evaluation;

import entity.catalog.ProgramType;
import entity.degree.Degree;
import entity.degree.ProgramCombinationRule;

import java.util.ArrayList;
import java.util.List;

public final class DegreeEvaluator {
    private final CompletionEvaluator completion;

    public DegreeEvaluator(CompletionEvaluator completion) {
        this.completion = completion;
    }

    public DegreeProgress evaluate(Degree degree, AcademicSnapshot snapshot, List<ProgramType> enrolledTypes,
                                   List<ProgramType> intendedTypes) {
        CompletionResult rows = completion.evaluate(degree.getCompletion(), snapshot);
        ProgramCombinationRule rule = degree.getProgramCombinations();

        if (rule.satisfiedBy(enrolledTypes)) {
            return new DegreeProgress(degree.getType(), rows, Status.MET, null);
        }

        List<ProgramType> planned = new ArrayList<>(enrolledTypes);
        planned.addAll(intendedTypes);

        if (rule.satisfiedBy(planned)) {
            return new DegreeProgress(degree.getType(), rows, Status.REVIEW,
                    "met once you're admitted to your intended programs");
        }

        return new DegreeProgress(degree.getType(), rows, Status.NOT_MET, "needs " + rule.describe());
    }
}
