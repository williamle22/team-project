package view.eligibility;

import interface_adapter.check_eligibility.CourseEligibilityView;
import interface_adapter.check_eligibility.EligibilityState;
import interface_adapter.check_eligibility.EligibilityViewModel;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Keeps the latest eligibility state for other views, such as plan cards, and tells them when it changes. Listeners run
 * on the event dispatch thread.
 */
public final class EligibilityView implements PropertyChangeListener {
    private final List<Runnable> listeners = new ArrayList<>();
    private EligibilityState state;

    public EligibilityView(EligibilityViewModel viewModel) {
        state = viewModel.getState();
        viewModel.addPropertyChangeListener(this);
    }

    /**
     * Returns the result for the planned course with ID {@code plannedCourseId}, or empty if the last check didn't
     * include it.
     */
    public Optional<CourseEligibilityView> get(UUID plannedCourseId) {
        return Optional.ofNullable(state.getCourses().get(plannedCourseId));
    }

    public EligibilityState getState() {
        return state;
    }

    /** Adds a listener that runs after each new state. */
    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(evt));
            return;
        }

        state = (EligibilityState) evt.getNewValue();

        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
