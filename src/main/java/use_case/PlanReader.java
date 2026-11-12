package use_case;

import entity.plan.Plan;

import java.util.Optional;
import java.util.UUID;

/** Read access to saved plans, for use cases that only look at plans. */
public interface PlanReader {
    /** Returns the saved plan with this id, or empty if there's none. */
    Optional<Plan> findPlan(UUID id);
}
