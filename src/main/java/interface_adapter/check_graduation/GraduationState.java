package interface_adapter.check_graduation;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class GraduationState {
    private UUID planId;
    private boolean projected;
    private String status = "";
    private String summary = "";
    private GraduationLineView degree;
    private GraduationLineView combination;
    private List<String> notes = List.of();
    private List<ProgramView> programs = List.of();
    private String error = "";

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public boolean isProjected() {
        return projected;
    }

    public void setProjected(boolean projected) {
        this.projected = projected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public GraduationLineView getDegree() {
        return degree;
    }

    public void setDegree(GraduationLineView degree) {
        this.degree = degree;
    }

    public GraduationLineView getCombination() {
        return combination;
    }

    public void setCombination(GraduationLineView combination) {
        this.combination = combination;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = List.copyOf(notes);
    }

    public List<ProgramView> getPrograms() {
        return programs;
    }

    public void setPrograms(List<ProgramView> programs) {
        this.programs = List.copyOf(programs);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraduationState)) {
            return false;
        }
        GraduationState that = (GraduationState) o;
        return projected == that.projected
                && Objects.equals(planId, that.planId)
                && Objects.equals(status, that.status)
                && Objects.equals(summary, that.summary)
                && Objects.equals(degree, that.degree)
                && Objects.equals(combination, that.combination)
                && Objects.equals(notes, that.notes)
                && Objects.equals(programs, that.programs)
                && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, projected, status, summary, degree, combination, notes, programs, error);
    }

    @Override
    public String toString() {
        return "GraduationState{" + planId + (projected ? ", whole plan" : ", completed only") + ", " + status
                + ", \"" + summary + "\", " + programs.size() + " programs, notes=" + notes + ", error=" + error
                + "}";
    }
}
