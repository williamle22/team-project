package data_access.catalog;

import entity.catalog.*;
import entity.requirement.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static data_access.catalog.JsonHelpers.*;

public class CatalogJsonMapper {
    private final RequirementJsonMapper requirements;

    public CatalogJsonMapper() {
        this.requirements = new RequirementJsonMapper();
    }

    public Requisite parseRequisite(String json) {
        if (json == null) {
            return null;
        }

        try {
            JSONObject obj = new JSONObject(json);
            return new Requisite(nullableNode(obj, "req"), strings(obj, "notes"), obj.getString("raw"));
        } catch (JSONException | IllegalArgumentException e) {
            throw new CatalogFormatException("bad requisite: " + json, e);
        }
    }

    public Exclusions parseExclusions(String json) {
        if (json == null) {
            return null;
        }

        try {
            JSONObject obj = new JSONObject(json);
            JSONArray data = obj.getJSONArray("items");
            List<ExclusionItem> items = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                JSONObject item = data.getJSONObject(i);
                String k = nullableString(item, "kind");
                TextKind kind = k == null ? null : TextKind.fromString(k);

                items.add(new ExclusionItem(
                        strings(item, "courses"), strings(item, "designators"), ints(item, "levels"), strings(item, "exclude"),
                        bool(item, "concurrent"), nullableString(item, "note"), kind, nullableString(item, "text")
                ));
            }

            return new Exclusions(items, strings(obj, "notes"), obj.getString("raw"));
        } catch (JSONException | IllegalArgumentException e) {
            throw new CatalogFormatException("bad exclusions: " + json, e);
        }
    }

    public List<BreadthCategory> parseBreadth(String json) {
        if (json == null) {
            return List.of();
        }

        try {
            JSONArray data = new JSONArray(json);
            List<BreadthCategory> output = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                output.add(BreadthCategory.fromInt(data.getInt(i)));
            }

            return output;
        } catch (JSONException | IllegalArgumentException e) {
            throw new CatalogFormatException("bad breadths: " + json, e);
        }
    }

    public List<String> parseAreas(String json) {
        if (json == null) {
            return List.of();
        }

        try {
            JSONArray data = new JSONArray(json);
            List<String> output = new ArrayList<>();

            for (int i = 0; i < data.length(); i++) {
                output.add(data.getString(i));
            }

            return output;
        } catch (JSONException | IllegalArgumentException e) {
            throw new CatalogFormatException("bad areas: " + json, e);
        }
    }

    public Sessions parseSessions(String json) {
        if (json == null) {
            return null;
        }

        try {
            JSONObject obj = new JSONObject(json);
            List<Section> fw = new ArrayList<>();
            List<Section> sm = new ArrayList<>();

            for (String s : strings(obj, "fall_winter")) {
                fw.add(Section.fromString(s));
            }
            for (String s : strings(obj, "summer")) {
                sm.add(Section.fromString(s));
            }

            return new Sessions(fw, sm);
        } catch (JSONException | IllegalArgumentException e) {
            throw new CatalogFormatException("bad sessions: " + json, e);
        }
    }

    public Enrolment parseEnrolment(String json) {
        if (json == null) {
            return null;
        }

        try {
            JSONObject obj = new JSONObject(json);
            EnrolmentKind kind = EnrolmentKind.fromString(obj.getString("kind"));
            Requirement req = nullableNode(obj, "req");
            List<ProgramReq> exclusions = new ArrayList<>();

            for (Requirement r : nodes(obj, "exclusions")) {
                if (!(r instanceof ProgramReq)) {
                    throw new CatalogFormatException("enrolment exclusion must be a program, was " + r);
                }

                exclusions.add((ProgramReq) r);
            }

            return new Enrolment(kind, req, exclusions, bool(obj, "first_round_only"), strings(obj, "notes"), obj.getString("raw"));
        } catch (JSONException | IllegalArgumentException e) {
            throw new CatalogFormatException("bad enrolment: " + json, e);
        }
    }

    public Completion parseCompletion(String json) {
        if (json == null) {
            return null;
        }

        try {
            JSONObject obj = new JSONObject(json);

            JSONArray rowData = obj.getJSONArray("requirements");
            List<CompletionRow> rows = new ArrayList<>();
            for (int i = 0; i < rowData.length(); i++) {
                rows.add(readRow(rowData.getJSONObject(i)));
            }

            List<CourseList> lists = new ArrayList<>();
            if (!obj.isNull("lists")) {
                JSONArray listData = obj.getJSONArray("lists");
                for (int i = 0; i < listData.length(); i++) {
                    lists.add(readList(listData.getJSONObject(i)));
                }
            }

            List<Requirement> exclusions = nodes(obj, "exclusions");

            return new Completion(rows, lists, exclusions, strings(obj, "notes"), obj.getString("raw"));
        } catch (JSONException | IllegalArgumentException e) {
            throw new CatalogFormatException("bad completion: " + json, e);
        }
    }

    private CompletionRow readRow(JSONObject row) {
        Requirement a = nullableNode(row, "audience");
        if (a != null && !(a instanceof ProfileReq)) {
            throw new CatalogFormatException("row audience must be a profile, was " + a);
        }

        return new CompletionRow(
                row.getInt("number"), RowKind.fromString(row.getString("kind")), nullableString(row, "label"), nullableString(row, "section"),
                nullableInt(row, "parent"), ints(row, "overlaps"), nullableNode(row, "req"), (ProfileReq) a,
                "transcript".equals(nullableString(row, "scope")), strings(row, "notes"), row.getString("raw")
        );
    }

    private CourseList readList(JSONObject list) {
        return new CourseList(list.getString("name"), nullableString(list, "title"), nodes(list, "items"));
    }

    private List<Requirement> nodes(JSONObject obj, String key) {
        if (obj.isNull(key)) {
            return List.of();
        }

        JSONArray data = obj.getJSONArray(key);
        List<Requirement> output = new ArrayList<>();

        for (int i = 0; i < data.length(); i++) {
            output.add(requirements.parseNode(data.getJSONObject(i)));
        }

        return output;
    }

    private Requirement nullableNode(JSONObject obj, String key) {
        return obj.isNull(key) ? null : requirements.parseNode(obj.getJSONObject(key));
    }
}
