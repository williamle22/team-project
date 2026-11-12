package entity.plan;

import entity.term.AcademicTerm;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CreditSummary {
    private final double completed;
    private final double transfer;
    private final double failed;
    private final double awaitingGrade;
    private final double inProgress;
    private final double planned;

    private CreditSummary(double completed, double transfer, double failed, double awaitingGrade,
                          double inProgress, double planned) {
        this.completed = completed;
        this.transfer = transfer;
        this.failed = failed;
        this.awaitingGrade = awaitingGrade;
        this.inProgress = inProgress;
        this.planned = planned;
    }

    public static CreditSummary of(Plan plan, CreditWeights weights, AcademicTerm current) {
        List<PlannedCourse> graded = plan.getCourses().stream()
                .filter(c -> c.getGrade().hasFinalGrade())
                .collect(Collectors.toList());
        Set<UUID> extras = GpaCalculator.extraAttempts(graded, GpaCalculator::finalGrade, weights);
        double transfer = plan.getTransferCredits().stream().mapToDouble(TransferCredit::getWeight).sum();
        double completed = transfer;
        double failed = 0;
        double awaitingGrade = 0;
        double inProgress = 0;
        double planned = 0;
        for (PlannedCourse c : plan.getCourses()) {
            double weight = weights.weightOf(c.getCode());
            if (c.getGrade().hasFinalGrade()) {
                if (extras.contains(c.getId())) {
                    continue;
                }
                if (c.getGrade().getFinalGrade().get() >= 50) {
                    completed += weight;
                } else {
                    failed += weight;
                }
            } else if (c.getPlacement().occupies(current)) {
                inProgress += weight;
            } else if (c.getPlacement().endTerm().isBefore(current)) {
                awaitingGrade += weight;
            } else {
                planned += weight;
            }
        }
        return new CreditSummary(completed, transfer, failed, awaitingGrade, inProgress, planned);
    }

    public double getCompleted() {
        return completed;
    }

    public double getTransfer() {
        return transfer;
    }

    public double getFailed() {
        return failed;
    }

    public double getAwaitingGrade() {
        return awaitingGrade;
    }

    public double getInProgress() {
        return inProgress;
    }

    public double getPlanned() {
        return planned;
    }

    public double total() {
        return completed + inProgress + planned + awaitingGrade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreditSummary)) {
            return false;
        }
        CreditSummary that = (CreditSummary) o;
        return Double.compare(completed, that.completed) == 0 && Double.compare(transfer, that.transfer) == 0
                && Double.compare(failed, that.failed) == 0 && Double.compare(awaitingGrade, that.awaitingGrade) == 0
                && Double.compare(inProgress, that.inProgress) == 0 && Double.compare(planned, that.planned) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(completed, transfer, failed, awaitingGrade, inProgress, planned);
    }

    @Override
    public String toString() {
        return "CreditSummary{completed " + completed + ", transfer " + transfer + ", failed " + failed
                + ", awaiting " + awaitingGrade + ", in progress " + inProgress + ", planned " + planned + "}";
    }
}
