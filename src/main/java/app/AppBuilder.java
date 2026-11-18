package app;

import javax.swing.*;

public class AppBuilder {
    public void build() {
        JFrame frame = new JFrame("DegreeExplorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // Add components here
        frame.add(new JLabel("Welcome to the application!", JLabel.CENTER));

        // Make the window visible
        frame.setVisible(true);
    }
}