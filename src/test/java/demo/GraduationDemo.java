package demo;

import com.formdev.flatlaf.FlatLightLaf;
import data_access.catalog.InMemoryCatalogRepository;
import data_access.catalog.SqliteCatalogLoader;
import data_access.degree.ResourceDegreeRepository;
import data_access.plan.InMemoryPlanRepository;
import entity.catalog.Section;
import entity.degree.DegreeType;
import entity.grades.CourseGrade;
import entity.plan.CreditWeights;
import entity.plan.GpaCalculator;
import entity.plan.Plan;
import entity.plan.PlannedCourse;
import entity.plan.ProgramStatus;
import entity.term.AcademicTerm;
import entity.term.Placement;
import interface_adapter.check_graduation.CheckGraduationController;
import interface_adapter.check_graduation.CheckGraduationPresenter;
import interface_adapter.check_graduation.GraduationViewModel;
import use_case.SnapshotFactory;
import use_case.check_graduation.CheckGraduationInteractor;
import view.graduation.GraduationPanel;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GraduationDemo {
    private static final Placement FALL_2025 = Placement.fallWinter(2025, Section.F);
    private static final Placement WINTER_2026 = Placement.fallWinter(2025, Section.S);
    private static final Placement YEAR_2025 = Placement.fallWinter(2025, Section.Y);
    private static final Placement FALL_2026 = Placement.fallWinter(2026, Section.F);
    private static final Placement WINTER_2027 = Placement.fallWinter(2026, Section.S);
    private static final Placement FALL_2027 = Placement.fallWinter(2027, Section.F);
    private static final Placement WINTER_2028 = Placement.fallWinter(2027, Section.S);
    private static final Placement FALL_2028 = Placement.fallWinter(2028, Section.F);
    private static final Placement WINTER_2029 = Placement.fallWinter(2028, Section.S);
    private static final Placement YEAR_2028 = Placement.fallWinter(2028, Section.Y);

    private final InMemoryPlanRepository plans = new InMemoryPlanRepository();
    private final Map<String, UUID> scenarios = new LinkedHashMap<>();
    private final JComboBox<String> scenario = new JComboBox<>();
    private final GraduationPanel graduation;
    private final JPanel panel = new JPanel(new BorderLayout());

    public GraduationDemo(Path db) {
        InMemoryCatalogRepository catalog = new SqliteCatalogLoader().load(db);
        GraduationViewModel viewModel = new GraduationViewModel();
        CheckGraduationInteractor interactor = new CheckGraduationInteractor(plans, catalog,
                new ResourceDegreeRepository(),
                new SnapshotFactory(catalog, new GpaCalculator(CreditWeights.FROM_CODE)),
                new CheckGraduationPresenter(viewModel));

        graduation = new GraduationPanel(viewModel, new CheckGraduationController(interactor));

        save("Ready to graduate: Philosophy Major, English and History Minors", readyToGraduate());
        save("CS Major, Math and Statistics Minors", fourYears());
        save("Intended Specialist and an unknown program", intendedAndUnknown());
        save("No degree chosen", noDegree());

        for (String name : scenarios.keySet()) {
            scenario.addItem(name);
        }
        scenario.addActionListener(e -> graduation.check(scenarios.get((String) scenario.getSelectedItem())));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        controls.add(new JLabel("Plan:"));
        controls.add(scenario);

        panel.add(controls, BorderLayout.NORTH);
        panel.add(graduation, BorderLayout.CENTER);

        graduation.check(scenarios.get(scenario.getItemAt(0)));
    }

    public JPanel getPanel() {
        return panel;
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Degree Explorer: Check Graduation Eligibility demo");
            frame.setContentPane(new GraduationDemo(Paths.get("data", "dgx.db")).getPanel());
            frame.setSize(1100, 800);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    private void save(String name, Plan plan) {
        plans.save(plan);
        scenarios.put(name, plan.getId());
    }

    private static Plan plan(String name) {
        Plan p = Plan.create(name);
        for (int year = 2025; year <= 2028; year++) {
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

    private static void addCourses(Plan p) {
        add(p, "CSC108H1", FALL_2025, 85);
        add(p, "CSC165H1", FALL_2025, 78);
        add(p, "PSY100H1", FALL_2025, 77);
        add(p, "MAT148H1", FALL_2025, 75);
        add(p, "ENG140Y1", YEAR_2025, 72);
        add(p, "CSC148H1", WINTER_2026, 82);
        add(p, "STA130H1", WINTER_2026, 80);
        add(p, "MAT149H1", WINTER_2026, 74);

        add(p, "CSC207H1", FALL_2026, null);
        add(p, "CSC258H1", FALL_2026, null);
        add(p, "STA237H1", FALL_2026, null);
        add(p, "MAT223H1", FALL_2026, null);
        add(p, "MAT235H1", FALL_2026, null);
        add(p, "CSC236H1", WINTER_2027, null);
        add(p, "CSC263H1", WINTER_2027, null);
        add(p, "STA238H1", WINTER_2027, null);
        add(p, "MAT224H1", WINTER_2027, null);
        add(p, "MAT236H1", WINTER_2027, null);

        add(p, "CSC209H1", FALL_2027, null);
        add(p, "CSC343H1", FALL_2027, null);
        add(p, "STA302H1", FALL_2027, null);
        add(p, "MAT301H1", FALL_2027, null);
        add(p, "ENG202H1", FALL_2027, null);
        add(p, "CSC369H1", WINTER_2028, null);
        add(p, "CSC373H1", WINTER_2028, null);
        add(p, "STA303H1", WINTER_2028, null);
        add(p, "MAT344H1", WINTER_2028, null);
        add(p, "SOC201H1", WINTER_2028, null);

        add(p, "CSC301H1", FALL_2028, null);
        add(p, "CSC311H1", FALL_2028, null);
        add(p, "CSC384H1", FALL_2028, null);
        add(p, "EEB202H1", FALL_2028, null);
        add(p, "ECO200Y1", YEAR_2028, null);
        add(p, "CSC309H1", WINTER_2029, null);
        add(p, "CSC443H1", WINTER_2029, null);
        add(p, "PHL245H1", WINTER_2029, null);
        add(p, "PHL275H1", WINTER_2029, null);
        add(p, "PSY220H1", WINTER_2029, null);
    }

    private static void graded(Plan p, int year, String... codes) {
        for (int i = 0; i < codes.length; i++) {
            Section section = codes[i].charAt(6) == 'Y' ? Section.Y : i % 2 == 0 ? Section.F : Section.S;
            add(p, codes[i], Placement.fallWinter(year, section), 75 + i);
        }
    }

    private static Plan readyToGraduate() {
        Plan p = plan("Ready to graduate");
        p.setDegree(DegreeType.HBA);
        p.addProgram("ASMAJ0231", ProgramStatus.ENROLLED);
        p.addProgram("ASMIN1645", ProgramStatus.ENROLLED);
        p.addProgram("ASMIN0652", ProgramStatus.ENROLLED);
        graded(p, 2025, "PHL100Y1", "ENG140Y1", "HIS103Y1", "MAT148H1", "MAT149H1", "PSY100H1", "ENG201H1",
                "ENG203H1");
        graded(p, 2026, "PHL205H1", "PHL206H1", "PHL217H1", "PHL232H1", "PHL245H1", "PHL275H1", "ENG202H1",
                "ENG210H1", "HIS200H1", "HIS205H1");
        graded(p, 2027, "ENG215H1", "ENG300H1", "PHL301H1", "PHL302H1", "PHL303H1", "PHL304H1", "SOC201H1",
                "EEB202H1", "HIS300H1", "HIS301H1");
        graded(p, 2028, "PHL400H1", "EEB208H1", "PSY220H1", "ENG301H1", "ENG302H1", "PHL305H1", "PHL306H1",
                "HIS302H1", "HIS304H1");
        return p;
    }

    private static Plan fourYears() {
        Plan p = plan("Four years");
        p.setDegree(DegreeType.HBSC);
        p.addProgram("ASMAJ1689", ProgramStatus.ENROLLED);
        p.addProgram("ASMIN1165", ProgramStatus.ENROLLED);
        p.addProgram("ASMIN2289", ProgramStatus.ENROLLED);
        addCourses(p);
        return p;
    }

    private static Plan intendedAndUnknown() {
        Plan p = plan("Intended and unknown");
        p.setDegree(DegreeType.HBSC);
        p.addProgram("ASSPE1689", ProgramStatus.INTENDED);
        p.addProgram("ASMAJ9999", ProgramStatus.ENROLLED);
        addCourses(p);
        return p;
    }

    private static Plan noDegree() {
        Plan p = plan("No degree");
        p.addProgram("ASMAJ1689", ProgramStatus.ENROLLED);
        addCourses(p);
        return p;
    }
}
