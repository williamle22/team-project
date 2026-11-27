package view.dgx;

import javax.swing.*;

public class AcademicTermPanel extends JPanel {

    public AcademicTermPanel(String term) {
        final JLabel title = new JLabel(term);


        this.add(title);
    }
}
