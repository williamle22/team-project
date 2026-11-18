package view;

import interface_adapter.sync_catalog.SyncCatalogController;
import interface_adapter.sync_catalog.SyncCatalogState;
import interface_adapter.sync_catalog.SyncCatalogState.Severity;
import interface_adapter.sync_catalog.SyncCatalogViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class CatalogStatusBar extends JPanel implements PropertyChangeListener {
    private static final Color WARNING_COLOUR = new Color(0xB26A00);
    private static final Color ERROR_COLOUR = new Color(0xC62828);

    private final JLabel message = new JLabel();
    private final JProgressBar progress = new JProgressBar();
    private final JButton retry = new JButton("Retry");

    public CatalogStatusBar(SyncCatalogViewModel viewModel, SyncCatalogController controller) {
        super(new FlowLayout(FlowLayout.LEFT));
        progress.setIndeterminate(true);
        retry.addActionListener(e -> controller.execute());
        add(message);
        add(progress);
        add(retry);
        viewModel.addPropertyChangeListener(this);
        redraw(viewModel.getState());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(evt));
            return;
        }
        redraw((SyncCatalogState) evt.getNewValue());
    }

    private void redraw(SyncCatalogState state) {
        message.setText(state.getMessage());
        message.setForeground(colourFor(state.getSeverity()));
        progress.setVisible(state.isLoading());
        retry.setVisible(state.isRetryAvailable());
    }

    private static Color colourFor(Severity severity) {
        switch (severity) {
            case WARNING:
                return WARNING_COLOUR;
            case ERROR:
                return ERROR_COLOUR;
            default:
                return UIManager.getColor("Label.foreground");
        }
    }
}
