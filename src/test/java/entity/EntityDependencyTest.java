package entity;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityDependencyTest {
    private static final Path ROOT = Paths.get("src", "main", "java", "entity");

    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "requirement", Set.of(),
            "catalog", Set.of("requirement"),
            "degree", Set.of("catalog", "requirement"),
            "term", Set.of("catalog"),
            "grades", Set.of(),
            "timetable", Set.of("catalog", "term"),
            "course_eval", Set.of("term"),
            "plan", Set.of("catalog", "term", "degree", "grades", "timetable"),
            "evaluation", Set.of("requirement", "catalog", "degree", "term"));

    private static final List<String> FORBIDDEN = List.of(
            "use_case.", "data_access.", "interface_adapter.", "view.", "app.",
            "org.json.", "java.sql.", "java.net.", "javax.swing.", "java.awt.", "lombok.");

    @Test
    void entityPackagesOnlyImportWhatTheGraphAllows() throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(ROOT)) {
            files = walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        }
        List<String> violations = new ArrayList<>();
        for (Path file : files) {
            Path relative = ROOT.relativize(file);
            if (relative.getNameCount() < 2) {
                continue;
            }
            String pkg = relative.getName(0).toString();
            if (!ALLOWED.containsKey(pkg)) {
                violations.add(relative + " is in entity." + pkg + ", which isn't in ALLOWED");
                continue;
            }
            for (String line : Files.readAllLines(file)) {
                String trimmed = line.strip();
                if (!trimmed.startsWith("import ")) {
                    continue;
                }
                String imported = trimmed.substring("import ".length())
                        .replace("static ", "").replace(";", "").strip();
                for (String bad : FORBIDDEN) {
                    if (imported.startsWith(bad)) {
                        violations.add(relative + " imports " + imported);
                    }
                }
                if (imported.startsWith("entity.")) {
                    String target = imported.split("\\.")[1];
                    if (!target.equals(pkg) && !ALLOWED.get(pkg).contains(target)) {
                        violations.add(relative + " imports " + imported);
                    }
                }
            }
        }
        assertTrue(violations.isEmpty(), String.join("\n", violations));
    }
}