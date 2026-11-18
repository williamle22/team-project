package view.dgx;

import javax.swing.*;
import java.awt.event.ActionListener;

public class CourseSearchPanel extends JPanel {
    private final JTextField searchField = new JTextField(15);

    public CourseSearchPanel() {
        final JLabel title = new JLabel("Search Course");

        ActionListener searchAction = e -> {
            System.out.println("SearchAction, TODO");
        };

        searchField.addActionListener(searchAction);

        this.add(title);
        this.add(searchField);
    }
}
