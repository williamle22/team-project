package view.dgx;

import interface_adapter.dgx.CourseSearchState;
import interface_adapter.dgx.CourseSearchViewModel;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.datatransfer.StringSelection;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Locale;

/** Course search panel with sample draggable results until catalog search is connected. */
public class CourseSearchPanel extends JPanel implements PropertyChangeListener {
    private final JTextField searchField = new JTextField(15);
    private final CourseSearchViewModel viewModel;
    private final DefaultListModel<String> resultModel = new DefaultListModel<>();
    private final JList<String> results = new JList<>(resultModel);

    public CourseSearchPanel(CourseSearchViewModel viewModel) {
        super(new BorderLayout(6, 6));
        this.viewModel = viewModel;
        setMinimumSize(new Dimension(200, 100));

        JLabel title = new JLabel("Search Course");
        JPanel header = new JPanel(new BorderLayout(4, 4));
        header.add(title, BorderLayout.NORTH);
        header.add(searchField, BorderLayout.CENTER);
        header.add(new JLabel("Results"), BorderLayout.SOUTH);

        results.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        results.setDragEnabled(true);
        results.setTransferHandler(new TransferHandler() {
            @Override
            protected java.awt.datatransfer.Transferable createTransferable(JComponent component) {
                String selected = results.getSelectedValue();
                return selected == null ? null : new StringSelection(selected);
            }

            @Override
            public int getSourceActions(JComponent component) {
                return COPY;
            }
        });

        searchField.addActionListener(e -> redraw(viewModel.getState()));
        viewModel.addPropertyChangeListener(this);
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(results), BorderLayout.CENTER);
        redraw(viewModel.getState());
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(event));
            return;
        }
        redraw((CourseSearchState) event.getNewValue());
    }

    private void redraw(CourseSearchState state) {
        String query = searchField.getText().trim().toUpperCase(Locale.ROOT);
        List<String> codes = state.getCourseCodes();
        resultModel.clear();
        for (String code : codes) {
            if (query.isEmpty() || code.toUpperCase(Locale.ROOT).contains(query)) {
                resultModel.addElement(code);
            }
        }
    }
}
