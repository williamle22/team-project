package use_case;

import entity.plan.Plan;

import java.util.List;
import java.util.UUID;

/** Read and write access to saved plans. */
public interface PlanRepository extends PlanReader {
    /** Returns a summary of each saved plan, most recently saved first. */
    List<PlanSummary> listPlans();

    /** Saves {@code plan}, creating or replacing the plan with its id. */
    void save(Plan plan);

    /** Deletes the plan with this id, or does nothing if there's no such plan. */
    void delete(UUID id);
}
