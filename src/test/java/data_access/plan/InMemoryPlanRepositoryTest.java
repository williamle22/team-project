package data_access.plan;

import entity.plan.Plan;
import org.junit.jupiter.api.Test;
import use_case.PlanSummary;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static entity.term.AcademicTerm.fall;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryPlanRepositoryTest {

    private static List<String> names(List<PlanSummary> summaries) {
        return summaries.stream().map(PlanSummary::getName).collect(Collectors.toList());
    }

    @Test
    void saveThenFind() {
        InMemoryPlanRepository repo = new InMemoryPlanRepository();
        Plan plan = Plan.create("My plan");
        plan.addTerm(fall(2026));
        repo.save(plan);
        assertEquals(plan, repo.findPlan(plan.getId()).get());
    }

    @Test
    void findMissingIsEmpty() {
        assertTrue(new InMemoryPlanRepository().findPlan(UUID.randomUUID()).isEmpty());
    }

    @Test
    void findReturnsACopy() {
        InMemoryPlanRepository repo = new InMemoryPlanRepository();
        Plan plan = Plan.create("My plan");
        repo.save(plan);
        repo.findPlan(plan.getId()).get().addTerm(fall(2026));
        assertTrue(repo.findPlan(plan.getId()).get().getTerms().isEmpty());
    }

    @Test
    void saveStoresACopy() {
        InMemoryPlanRepository repo = new InMemoryPlanRepository();
        Plan plan = Plan.create("My plan");
        repo.save(plan);
        plan.rename("Unsaved name");
        assertEquals("My plan", repo.findPlan(plan.getId()).get().getName());
    }

    @Test
    void listNewestFirst() {
        InMemoryPlanRepository repo = new InMemoryPlanRepository();
        Plan first = Plan.create("First");
        Plan second = Plan.create("Second");
        repo.save(first);
        repo.save(second);
        assertEquals(List.of("Second", "First"), names(repo.listPlans()));
        first.rename("First, edited");
        repo.save(first);
        assertEquals(List.of("First, edited", "Second"), names(repo.listPlans()));
    }

    @Test
    void summaryHasIdAndSaveTime() {
        InMemoryPlanRepository repo = new InMemoryPlanRepository();
        Plan plan = Plan.create("My plan");
        repo.save(plan);
        PlanSummary summary = repo.listPlans().get(0);
        assertEquals(plan.getId(), summary.getId());
        assertNotNull(summary.getLastModified());
    }

    @Test
    void deleteRemoves() {
        InMemoryPlanRepository repo = new InMemoryPlanRepository();
        Plan plan = Plan.create("My plan");
        repo.save(plan);
        repo.delete(plan.getId());
        assertTrue(repo.findPlan(plan.getId()).isEmpty());
        assertTrue(repo.listPlans().isEmpty());
    }

    @Test
    void deleteMissingIsNoOp() {
        InMemoryPlanRepository repo = new InMemoryPlanRepository();
        repo.save(Plan.create("My plan"));
        repo.delete(UUID.randomUUID());
        assertEquals(1, repo.listPlans().size());
    }
}
