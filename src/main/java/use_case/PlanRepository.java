package use_case;

import entity.plan.Plan;

import java.util.List;
import java.util.UUID;

public interface PlanRepository extends PlanReader {
    List<PlanSummary> listPlans();

    void save(Plan plan);

    void delete(UUID id);
}
