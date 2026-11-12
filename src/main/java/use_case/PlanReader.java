package use_case;

import entity.plan.Plan;

import java.util.Optional;
import java.util.UUID;

public interface PlanReader {
    Optional<Plan> findPlan(UUID id);
}
