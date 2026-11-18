package view.dgx;

import entity.plan.Plan;

import javax.swing.*;

public class AcademicPlannerPanel extends JPanel {
    // TODO: may need view model later, if we need more custom serialization, for now i think just working off the entity model is sufficient
    private Plan plan;

    public AcademicPlannerPanel(Plan plan) {
        this.plan = plan;
        final JLabel title = new JLabel("Academic Planner");
        final JButton addTermButton = new JButton("Add Term");
        addTermButton.addActionListener(e -> {
            System.out.println("Add term button clicked");
        });



        this.add(title);
        this.add(addTermButton);
    }
}
