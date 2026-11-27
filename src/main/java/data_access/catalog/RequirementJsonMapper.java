package data_access.catalog;

import entity.requirement.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static data_access.catalog.JsonHelpers.*;

/** Converts dgx-backend JSON requirement nodes into {@link Requirement} trees. */
public final class RequirementJsonMapper {
    /**
     * Parses a node and its children.
     *
     * @throws CatalogFormatException if the node or a child has an unknown type, a missing or mistyped field, or an
     *     invalid value. The message describes the innermost node that failed.
     */
    public Requirement parseNode(JSONObject obj) {
        try {
            String type = obj.getString("type");
            boolean c = obj.optBoolean("concurrent", false);
            switch (type) {
                case "and":
                    return new AndReq(parseItems(obj), nullableDouble(obj, "weight"), c);
                case "or":
                    return new OrReq(parseItems(obj), c);
                case "not":
                    return new NotReq(parseItems(obj).get(0), c);
                case "choose":
                    return new ChooseReq(obj.getInt("count"), parseItems(obj), c);
                case "course":
                    return new CourseReq(obj.getString("code"), nullableInt(obj, "min_grade"), c);
                case "credits":
                    return new CreditsReq(obj.optDouble("amount", 0), nullableDouble(obj, "max_amount"), nullableDouble(obj, "weight"), parseFilter(obj, true), c);
                case "average":
                    return new AverageReq(obj.getInt("min_grade"), nullableDouble(obj, "amount"), bool(obj, "best"), parseFilter(obj, false), parseItems(obj), c);
                case "gpa":
                    return new GpaReq(obj.getDouble("min_gpa"), nullableString(obj, "scope"), c);
                case "program":
                    return new ProgramReq(strings(obj, "programs"), obj.getString("text"), c);
                case "requirement":
                    return new RowRefReq(ints(obj, "requirements"), c);
                case "profile":
                    return new ProfileReq(obj.getString("attr"), normalizedValue(obj), obj.getString("text"), c);
                case "text":
                    return new TextReq(TextKind.fromString(obj.getString("kind")), obj.getString("text"), c);
                default:
                    throw new CatalogFormatException("unknown requirement node type: " + type);
            }
        } catch (JSONException | IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new CatalogFormatException("bad node: " + obj, e);
        }
    }

    private List<Requirement> parseItems(JSONObject obj) {
        if (obj.isNull("items")) {
            return List.of();
        }

        JSONArray data = obj.getJSONArray("items");
        List<Requirement> output = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            output.add(parseNode(data.getJSONObject(i)));
        }

        return output;
    }

    private CourseFilter parseFilter(JSONObject obj, boolean includeMinGrade) {
        CourseFilter.Builder b = CourseFilter.builder();

        b.courses(strings(obj, "courses"));
        b.designators(strings(obj, "designators"));
        b.lists(strings(obj, "lists"));
        b.exclude(strings(obj, "exclude"));
        b.excludeDesignators(strings(obj, "exclude_designators"));
        b.levels(ints(obj, "levels"));
        b.breadth(ints(obj, "breadth"));
        b.requirements(ints(obj, "requirements"));

        Double max = nullableDouble(obj, "max_course_amount");
        if (max != null) {
            b.maxCourseAmount(max);
        }

        if (includeMinGrade) {
            Integer min = nullableInt(obj, "min_grade");
            if (min != null) {
                b.minGrade(min);
            }
        }

        b.transfer(bool(obj, "transfer"));
        b.crNcr(bool(obj, "cr_ncr"));
        b.perDesignator(bool(obj, "per_designator"));

        return b.build();
    }

    private Object normalizedValue(JSONObject obj) {
        Object v = obj.get("value");
        return v instanceof Number ? ((Number) v).intValue() : v;
    }
}
