package view;

import entity.catalog.BreadthCategory;
import entity.catalog.Campus;
import interface_adapter.search_courses.SearchCoursesController;
import interface_adapter.search_courses.SearchCoursesState;
import interface_adapter.search_courses.SearchCoursesViewModel;
import interface_adapter.search_courses.SearchResultRow;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The search sidebar for Use Case 2: a search box, optional filters, and the list of matching courses. It searches
 * 200 ms after the user stops typing, and right away when a filter changes.
 */
public final class SearchPanel extends JPanel implements PropertyChangeListener {
    private static final int DEBOUNCE_MS = 200;
    private static final int[] LEVELS = {100, 200, 300, 400};
    private static final String LIST_CARD = "list";
    private static final String MESSAGE_CARD = "message";

    private final SearchCoursesController controller;

    private final JTextField query = new JTextField();
    private final JTextField designator = new JTextField(3);
    private final JCheckBox[] levels = new JCheckBox[LEVELS.length];
    private final JComboBox<String> campus = new JComboBox<>();
    private final JCheckBox[] breadth = new JCheckBox[BreadthCategory.values().length];

    private final JList<SearchResultRow> list = new JList<>();
    private final JLabel message = new JLabel("", SwingConstants.CENTER);
    private final JLabel footer = new JLabel();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Timer debounce = new Timer(DEBOUNCE_MS, e -> search());

    private Consumer<String> onCourseSelected = code -> {
    };
    private Consumer<String> onCourseActivated = code -> {
    };

    public SearchPanel(SearchCoursesViewModel viewModel, SearchCoursesController controller) {
        super(new BorderLayout());
        this.controller = controller;

        debounce.setRepeats(false);
        query.putClientProperty("JTextField.placeholderText", "Search courses");
        query.putClientProperty("JTextField.showClearButton", true);
        query.getDocument().addDocumentListener(onEdit(debounce::restart));
        designator.getDocument().addDocumentListener(onEdit(debounce::restart));

        JPanel filters = filtersPanel();
        filters.setVisible(false);
        JToggleButton filtersButton = new JToggleButton("Filters");
        filtersButton.addActionListener(e -> {
            filters.setVisible(filtersButton.isSelected());
            revalidate();
        });

        JPanel searchRow = new JPanel(new BorderLayout(4, 0));
        searchRow.add(query, BorderLayout.CENTER);
        searchRow.add(filtersButton, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.add(searchRow, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new RowRenderer());
        list.setPrototypeCellValue(new SearchResultRow("CSC148H1", "A title", "0.5", "UTSG"));
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && list.getSelectedValue() != null) {
                onCourseSelected.accept(list.getSelectedValue().getCode());
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    activateSelected();
                }
            }
        });
        list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "activate");
        list.getActionMap().put("activate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateSelected();
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(scroll, LIST_CARD);
        content.add(message, MESSAGE_CARD);

        footer.setFont(footer.getFont().deriveFont(footer.getFont().getSize2D() - 1f));
        footer.setForeground(UIManager.getColor("Label.disabledForeground"));

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setPreferredSize(new Dimension(360, 0));
        add(top, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        viewModel.addPropertyChangeListener(this);
        redraw(viewModel.getState());
        search();
    }

    /** Sets what happens when the user selects a course. The listener receives the course code. */
    public void setOnCourseSelected(Consumer<String> listener) {
        onCourseSelected = listener;
    }

    /**
     * Sets what happens when the user double-clicks a course or presses Enter on it. The listener receives the course
     * code.
     */
    public void setOnCourseActivated(Consumer<String> listener) {
        onCourseActivated = listener;
    }

    /** Runs the current search again on the event dispatch thread. Call it when the catalog changes. */
    public void refresh() {
        SwingUtilities.invokeLater(this::search);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> propertyChange(evt));
            return;
        }
        redraw((SearchCoursesState) evt.getNewValue());
    }

    private JPanel filtersPanel() {
        JPanel designatorRow = row("Designator");
        designatorRow.add(designator);

        JPanel levelRow = row("Level");
        for (int i = 0; i < LEVELS.length; i++) {
            levels[i] = new JCheckBox(String.valueOf(LEVELS[i]));
            levels[i].addActionListener(e -> search());
            levelRow.add(levels[i]);
        }

        JPanel campusRow = row("Campus");
        campus.addItem("Any");
        for (Campus c : Campus.values()) {
            campus.addItem(c.name());
        }
        campus.addActionListener(e -> search());
        campusRow.add(campus);

        JPanel breadthRow = row("Breadth");
        BreadthCategory[] categories = BreadthCategory.values();
        for (int i = 0; i < categories.length; i++) {
            breadth[i] = new JCheckBox(String.valueOf(categories[i].getNumber()));
            breadth[i].setToolTipText(categories[i].getDisplayName());
            breadth[i].addActionListener(e -> search());
            breadthRow.add(breadth[i]);
        }

        JPanel filters = new JPanel(new GridLayout(0, 1));
        filters.add(designatorRow);
        filters.add(levelRow);
        filters.add(campusRow);
        filters.add(breadthRow);
        return filters;
    }

    private static JPanel row(String label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        JLabel name = new JLabel(label);
        name.setPreferredSize(new Dimension(70, name.getPreferredSize().height));
        row.add(name);
        return row;
    }

    private void search() {
        Set<Integer> selectedLevels = new HashSet<>();
        for (int i = 0; i < LEVELS.length; i++) {
            if (levels[i].isSelected()) {
                selectedLevels.add(LEVELS[i]);
            }
        }

        Set<String> selectedCampuses = new HashSet<>();
        int campusIndex = campus.getSelectedIndex();
        if (campusIndex > 0) {
            selectedCampuses.add(Campus.values()[campusIndex - 1].getValue());
        }

        Set<Integer> selectedBreadth = new HashSet<>();
        BreadthCategory[] categories = BreadthCategory.values();
        for (int i = 0; i < categories.length; i++) {
            if (breadth[i].isSelected()) {
                selectedBreadth.add(categories[i].getNumber());
            }
        }

        controller.execute(query.getText(), designator.getText(), selectedLevels, selectedCampuses, selectedBreadth);
    }

    private void redraw(SearchCoursesState state) {
        list.setListData(state.getResults().toArray(new SearchResultRow[0]));
        message.setText(state.getMessage());
        cards.show(content, state.getMessage().isEmpty() ? LIST_CARD : MESSAGE_CARD);
        footer.setText(state.getFooter());
        footer.setVisible(!state.getFooter().isEmpty());
    }

    private void activateSelected() {
        if (list.getSelectedValue() != null) {
            onCourseActivated.accept(list.getSelectedValue().getCode());
        }
    }

    private static DocumentListener onEdit(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    private static final class RowRenderer extends JPanel implements ListCellRenderer<SearchResultRow> {
        private final JLabel code = new JLabel();
        private final JLabel credits = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel details = new JLabel();

        RowRenderer() {
            super(new GridLayout(0, 1));
            code.setFont(code.getFont().deriveFont(Font.BOLD));
            details.setFont(details.getFont().deriveFont(details.getFont().getSize2D() - 1f));

            JPanel firstLine = new JPanel(new BorderLayout());
            firstLine.setOpaque(false);
            firstLine.add(code, BorderLayout.WEST);
            firstLine.add(credits, BorderLayout.EAST);

            add(firstLine);
            add(title);
            add(details);
            setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends SearchResultRow> list, SearchResultRow row,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            code.setText(row.getCode());
            credits.setText(row.getCredits());
            title.setText(row.getTitle());
            details.setText(row.getDetails());

            Color foreground = isSelected ? list.getSelectionForeground() : list.getForeground();
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            code.setForeground(foreground);
            credits.setForeground(foreground);
            title.setForeground(foreground);
            details.setForeground(isSelected ? foreground : UIManager.getColor("Label.disabledForeground"));
            return this;
        }
    }
}
