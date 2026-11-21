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

public final class EligibilityView implements PropertyChangeListener {
    private final List<Runnable> listeners = new ArrayList<>();
    private EligibilityState state;

    public EligibilityView(EligibilityViewModel viewModel) {
        state = viewModel.getState();
        viewModel.addPropertyChangeListener(this);
    }

    public Optional<CourseEligibilityView> get(UUID plannedCourseId) {
        return Optional.ofNullable(state.getCourses().get(plannedCourseId));
    }

    public EligibilityState getState() {
        return state;
    }

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
