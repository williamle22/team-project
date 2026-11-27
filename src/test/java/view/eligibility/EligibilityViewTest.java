package view.eligibility;

import interface_adapter.check_eligibility.CourseEligibilityView;
import interface_adapter.check_eligibility.EligibilityState;
import interface_adapter.check_eligibility.EligibilityViewModel;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EligibilityViewTest {
    private static final CourseEligibilityView CSC148 =
            new CourseEligibilityView(UUID.randomUUID(), "CSC148H1", "met", "Requirements met", List.of(), List.of());

    private static void publish(EligibilityViewModel viewModel) {
        EligibilityState state = new EligibilityState();
        state.setCourses(Map.of(CSC148.getPlannedCourseId(), CSC148));
        viewModel.setState(state);
        viewModel.firePropertyChanged();
    }

    @Test
    void startsWithViewModelState() {
        EligibilityViewModel viewModel = new EligibilityViewModel();

        EligibilityView view = new EligibilityView(viewModel);

        assertSame(viewModel.getState(), view.getState());
        assertEquals(Optional.empty(), view.get(CSC148.getPlannedCourseId()));
    }

    @Test
    void updatesOnTheSwingThreadAndNotifies() throws Exception {
        EligibilityViewModel viewModel = new EligibilityViewModel();
        EligibilityView view = new EligibilityView(viewModel);
        List<Boolean> onSwingThread = new ArrayList<>();
        view.addChangeListener(() -> onSwingThread.add(SwingUtilities.isEventDispatchThread()));

        publish(viewModel);
        SwingUtilities.invokeAndWait(() -> { });

        assertEquals(List.of(true), onSwingThread);
        assertEquals(Optional.of(CSC148), view.get(CSC148.getPlannedCourseId()));
    }

    @Test
    void updatesRightAwayOnTheSwingThread() throws Exception {
        EligibilityViewModel viewModel = new EligibilityViewModel();
        EligibilityView view = new EligibilityView(viewModel);
        List<Boolean> updated = new ArrayList<>();

        SwingUtilities.invokeAndWait(() -> {
            publish(viewModel);
            updated.add(view.get(CSC148.getPlannedCourseId()).isPresent());
        });

        assertTrue(updated.get(0));
    }
}
