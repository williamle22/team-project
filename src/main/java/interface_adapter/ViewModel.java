package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Observable state for one view. A presenter calls {@link #setState} and then {@link #firePropertyChanged()}, and every
 * listener receives the new state.
 *
 * @param <T> the state type
 */
public abstract class ViewModel<T> {
    private final String viewName;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private T state;

    public ViewModel(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public T getState() {
        return state;
    }

    public void setState(T state) {
        this.state = state;
    }

    /**
     * Notifies listeners under the property name {@code "state"}. It always fires, even if the state object hasn't
     * changed.
     */
    public void firePropertyChanged() {
        support.firePropertyChange("state", null, state);
    }

    /** Notifies listeners under {@code propertyName}, for events that a view handles differently from a redraw. */
    public void firePropertyChanged(String propertyName) {
        support.firePropertyChange(propertyName, null, state);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
}
