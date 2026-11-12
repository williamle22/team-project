package data_access.plan;

import entity.plan.Plan;
import use_case.PlanRepository;
import use_case.PlanSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryPlanRepository implements PlanRepository {
    private final Map<UUID, Plan> plans = new LinkedHashMap<>();
    private final Map<UUID, Instant> savedAt = new HashMap<>();

    @Override
    public Optional<Plan> findPlan(UUID id) {
        return Optional.ofNullable(plans.get(id)).map(Plan::copy);
    }

    @Override
    public List<PlanSummary> listPlans() {
        List<PlanSummary> summaries = new ArrayList<>();
        for (Plan plan : plans.values()) {
            summaries.add(new PlanSummary(plan.getId(), plan.getName(), savedAt.get(plan.getId())));
        }
        Collections.reverse(summaries);
        return summaries;
    }

    @Override
    public void save(Plan plan) {
        plans.remove(plan.getId());
        plans.put(plan.getId(), plan.copy());
        savedAt.put(plan.getId(), Instant.now());
    }

    @Override
    public void delete(UUID id) {
        plans.remove(id);
        savedAt.remove(id);
    }
}
