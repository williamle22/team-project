package data_access.degree;

import data_access.catalog.CatalogJsonMapper;
import entity.catalog.Completion;
import entity.catalog.ProgramType;
import entity.degree.Degree;
import entity.degree.DegreeType;
import entity.degree.ProgramCombinationRule;
import org.json.JSONArray;
import org.json.JSONObject;
import use_case.DegreeReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads degree rules from JSON files on the classpath. Each file holds one degree's completion rows, in the catalog's
 * completion format, and its program combinations. The no-argument constructor loads HBA, HBSc, and BCom from
 * {@code src/main/resources/degrees}.
 */
public final class ResourceDegreeRepository implements DegreeReader {
    private final Map<DegreeType, Degree> degrees = new EnumMap<>(DegreeType.class);

    public ResourceDegreeRepository() {
        this("/degrees/hba.json", "/degrees/hbsc.json", "/degrees/bcom.json");
    }

    /**
     * Loads each classpath resource in {@code resources}, such as {@code "/degrees/hba.json"}.
     *
     * @throws IllegalStateException if a resource doesn't exist
     */
    public ResourceDegreeRepository(String... resources) {
        CatalogJsonMapper mapper = new CatalogJsonMapper();

        for (String resource : resources) {
            Degree degree = parse(new JSONObject(read(resource)), mapper);
            degrees.put(degree.getType(), degree);
        }
    }

    @Override
    public Optional<Degree> findDegree(DegreeType type) {
        return Optional.ofNullable(degrees.get(type));
    }

    @Override
    public List<Degree> allDegrees() {
        return List.copyOf(degrees.values());
    }

    private static String read(String resource) {
        try (InputStream in = ResourceDegreeRepository.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("missing degree file " + resource);
            }

            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Degree parse(JSONObject obj, CatalogJsonMapper mapper) {
        DegreeType type = DegreeType.valueOf(obj.getString("degree"));
        Completion completion = mapper.parseCompletion(obj.getJSONObject("completion").toString());
        List<Map<ProgramType, Integer>> combinations = new ArrayList<>();
        JSONArray array = obj.getJSONArray("program_combinations");
        for (int i = 0; i < array.length(); i++) {
            JSONObject combination = array.getJSONObject(i);
            Map<ProgramType, Integer> counts = new EnumMap<>(ProgramType.class);

            for (String key : combination.keySet()) {
                counts.put(ProgramType.fromString(key), combination.getInt(key));
            }

            combinations.add(counts);
        }

        List<String> eligible = new ArrayList<>();
        JSONArray codes = obj.optJSONArray("eligible_programs");

        if (codes != null) {
            for (int i = 0; i < codes.length(); i++) {
                eligible.add(codes.getString(i));
            }
        }

        return new Degree(type, completion, new ProgramCombinationRule(combinations), eligible);
    }
}
