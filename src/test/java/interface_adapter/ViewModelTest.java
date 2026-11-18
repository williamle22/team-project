package interface_adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewModelTest {
    private final ViewModel<String> viewModel = new ViewModel<>("test view") {};
    private final List<PropertyChangeEvent> events = new ArrayList<>();

    @BeforeEach
    void setUp() {
        viewModel.addPropertyChangeListener(events::add);
    }

    @Test
    void keepsViewNameAndState() {
        viewModel.setState("loading");
        assertEquals("test view", viewModel.getViewName());
        assertEquals("loading", viewModel.getState());
    }

    @Test
    void firesStateOnEveryCall() {
        viewModel.setState("loading");
        viewModel.firePropertyChanged();
        viewModel.firePropertyChanged();
        assertEquals(2, events.size());
        assertEquals("state", events.get(1).getPropertyName());
        assertEquals("loading", events.get(1).getNewValue());
    }

    @Test
    void firesUnderGivenName() {
        viewModel.setState("loading");
        viewModel.firePropertyChanged("error");
        assertEquals(1, events.size());
        assertEquals("error", events.get(0).getPropertyName());
        assertEquals("loading", events.get(0).getNewValue());
    }
}
