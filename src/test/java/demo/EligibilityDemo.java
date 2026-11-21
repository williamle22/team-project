package demo;

import com.formdev.flatlaf.FlatLightLaf;
import data_access.catalog.InMemoryCatalogRepository;
import data_access.catalog.SqliteCatalogLoader;
import data_access.plan.InMemoryPlanRepository;
import entity.catalog.Section;
import entity.evaluation.EligibilityEvaluator;
import entity.grades.CourseGrade;
import entity.plan.CreditWeights;
import entity.plan.GpaCalculator;
import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.plan.TransferCredit;
import entity.term.AcademicTerm;
import entity.term.Placement;
import interface_adapter.check_eligibility.CheckEligibilityController;
import interface_adapter.check_eligibility.CheckEligibilityPresenter;
import interface_adapter.check_eligibility.CourseEligibilityView;
import interface_adapter.check_eligibility.EligibilityViewModel;
import use_case.CurrentAcademicYear;
import use_case.SnapshotFactory;
import use_case.check_eligibility.CheckEligibilityInteractor;
import view.eligibility.EligibilityTreePanel;
import view.eligibility.EligibilityView;
import view.eligibility.StatusIcon;
import view.eligibility.WarningList;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class EligibilityDemo {
    private static final Placement FALL_2025 = Placement.fallWinter(2025, Section.F);
    private static final Placement WINTER_2026 = Placement.fallWinter(2025, Section.S);
    private static final Placement SUMMER_2026 = Placement.summer(2026, Section.F);
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final Placement FALL_2027 = Placement.fallWinter(2027, Section.F);
    private static final Placement WINTER_2028 = Placement.fallWinter(2027, Section.S);

    private final InMemoryPlanRepository plans = new InMemoryPlanRepository();
    private final Map<String, UUID> scenarios = new LinkedHashMap<>();
    private final CheckEligibilityController controller;
    private final EligibilityView eligibility;

    private final JComboBox<String> scenario = new JComboBox<>();
    private final WarningList planWarnings = new WarningList();
    private final JLabel error = new JLabel();
    private final DefaultListModel<PlannedCourse> courses = new DefaultListModel<>();
    private final JList<PlannedCourse> courseList = new JList<>(courses);
    private final EligibilityTreePanel details = new EligibilityTreePanel();
    private final JPanel panel = new JPanel(new BorderLayout());

    private UUID planId;

    public EligibilityDemo(Path db) {
        InMemoryCatalogRepository catalog = new SqliteCatalogLoader().load(db);
        EligibilityViewModel viewModel = new EligibilityViewModel();
        CheckEligibilityInteractor interactor = new CheckEligibilityInteractor(plans, catalog,
                new SnapshotFactory(catalog, new GpaCalculator(CreditWeights.FROM_CODE)),
                new CurrentAcademicYear(Clock.systemDefaultZone()), new EligibilityEvaluator(),
                new CheckEligibilityPresenter(viewModel));

        controller = new CheckEligibilityController(interactor);
        eligibility = new EligibilityView(viewModel);
        eligibility.addChangeListener(this::redraw);

        save("On track: first two years of CS", onTrack());
        save("Problems: every kind of warning", problems());

        buildUi();
        scenario.setSelectedIndex(0);
        open(scenarios.get(scenario.getItemAt(0)));
    }

    public JPanel getPanel() {
        return panel;
    }

    public JList<PlannedCourse> getCourseList() {
        return courseList;
    }

    public JComboBox<String> getScenario() {
        return scenario;
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Degree Explorer: Check Eligibility demo");
            frame.setContentPane(new EligibilityDemo(Paths.get("data", "dgx.db")).getPanel());
            frame.setSize(1150, 720);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private void buildUi() {
        for (String name : scenarios.keySet()) {
            scenario.addItem(name);
        }
        scenario.addActionListener(e -> open(scenarios.get((String) scenario.getSelectedItem())));

        JButton grade = new JButton("Set final grade...");
        grade.addActionListener(e -> setGrade());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.add(new JLabel("Plan:"));
        controls.add(scenario);
        controls.add(grade);

        error.setForeground(new Color(0xC62828));

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        top.add(controls, BorderLayout.NORTH);
        top.add(planWarnings, BorderLayout.CENTER);
        top.add(error, BorderLayout.SOUTH);

        courseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        courseList.setCellRenderer(new CourseRenderer());
        courseList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });

        JScrollPane list = new JScrollPane(courseList);
        list.setPreferredSize(new Dimension(380, 0));

        panel.add(top, BorderLayout.NORTH);
        panel.add(list, BorderLayout.WEST);
        panel.add(details, BorderLayout.CENTER);
    }

    private void open(UUID id) {
        planId = id;
        reloadCourses(0);
        controller.execute(planId);
    }

    private void reloadCourses(int selected) {
        courses.clear();
        for (PlannedCourse c : plans.findPlan(planId).get().getCourses()) {
            courses.addElement(c);
        }
        courseList.setSelectedIndex(selected);
    }

    private void setGrade() {
        PlannedCourse selected = courseList.getSelectedValue();
        if (selected == null) {
            return;
        }

        String input = JOptionPane.showInputDialog(panel, "Final grade for " + selected.getCode()
                + " (0-100, blank to clear):", selected.getGrade().getFinalGrade().map(String::valueOf).orElse(""));
        if (input == null) {
            return;
        }

        Integer finalGrade;
        try {
            finalGrade = input.isBlank() ? null : Integer.valueOf(input.strip());
            Plan plan = plans.findPlan(planId).get();
            plan.replaceCourse(selected.withGrade(selected.getGrade().withFinalGrade(finalGrade)));
            plans.save(plan);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panel, "Enter a whole number from 0 to 100.");
            return;
        }

        reloadCourses(courseList.getSelectedIndex());
        controller.execute(planId);
    }

    private void redraw() {
        planWarnings.setWarnings(eligibility.getState().getPlanWarnings());
        error.setText(eligibility.getState().getError());
        courseList.repaint();
        showSelected();
    }

    private void showSelected() {
        PlannedCourse selected = courseList.getSelectedValue();
        Optional<CourseEligibilityView> view = selected == null ? Optional.empty()
                : eligibility.get(selected.getId());

        if (view.isPresent()) {
            details.show(view.get());
        } else {
            details.clear();
        }
    }

    private void save(String name, Plan plan) {
        plans.save(plan);
        scenarios.put(name, plan.getId());
    }

    private static Plan plan(String name) {
        Plan p = Plan.create(name);
        for (int year = 2025; year <= 2027; year++) {
            p.addTerm(AcademicTerm.fall(year));
            p.addTerm(AcademicTerm.winter(year + 1));
            p.addTerm(AcademicTerm.summer(year + 1));
        }
        return p;
    }

    private static void add(Plan p, String code, Placement placement, Integer grade) {
        PlannedCourse c = p.addCourse(code, placement);
        if (grade != null) {
            p.replaceCourse(c.withGrade(new CourseGrade(grade, List.of())));
        }
    }

    private static Plan onTrack() {
        Plan p = plan("On track");
        add(p, "CSC108H1", FALL_2025, 85);
        add(p, "MAT135H1", FALL_2025, 78);
        add(p, "MAT223H1", FALL_2025, 75);
        add(p, "CSC148H1", WINTER_2026, 82);
        add(p, "CSC165H1", WINTER_2026, 76);
        add(p, "MAT136H1", WINTER_2026, 70);
        add(p, "CSC207H1", FALL_2026, null);
        add(p, "CSC236H1", FALL_2026, null);
        add(p, "STA237H1", FALL_2026, null);
        add(p, "CSC209H1", WINTER_2027, null);
        add(p, "CSC263H1", FALL_2027, null);
        add(p, "CSC373H1", WINTER_2028, null);
        return p;
    }

    private static Plan problems() {
        Plan p = plan("Problems");
        add(p, "CSC108H1", FALL_2025, 45);
        add(p, "MAT135H1", FALL_2025, 80);
        add(p, "CSC148H1", WINTER_2026, 55);
        add(p, "MAT136H1", WINTER_2026, 72);
        add(p, "CSC108H1", SUMMER_2026, 76);
        add(p, "CSC207H1", FALL_2026, null);
        add(p, "XYZ999H1", FALL_2026, null);
        add(p, "CSC111H1", WINTER_2027, null);
        add(p, "CSC165H1", WINTER_2027, null);
        add(p, "CSC236H1", WINTER_2027, null);
        add(p, "CSC263H1", FALL_2027, null);
        p.addTransferCredit(new TransferCredit("MAT1**", 0.5));
        return p;
    }

    private final class CourseRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected,
                                                      boolean focused) {
            super.getListCellRendererComponent(list, value, index, selected, focused);

            PlannedCourse c = (PlannedCourse) value;
            Optional<CourseEligibilityView> view = eligibility.get(c.getId());
            String grade = c.getGrade().getFinalGrade().map(g -> " · " + g + "%").orElse("");
            String summary = view.map(CourseEligibilityView::getSummary).orElse("Checking...");

            setIcon(new StatusIcon(view.map(CourseEligibilityView::getStatus).orElse(""), 12));
            setIconTextGap(10);
            setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            setText("<html><b>" + c.getCode() + "</b> · " + c.getPlacement() + grade
                    + "<br><font color='#757575'>" + summary.replace("&", "&amp;").replace("<", "&lt;")
                    + "</font></html>");

            return this;
        }
    }
}
