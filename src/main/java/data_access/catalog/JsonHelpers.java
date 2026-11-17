package data_access.catalog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

final class JsonHelpers {
    private JsonHelpers() {}

    static List<String> strings(JSONObject o, String key) {
        if (o.isNull(key)) {
            return List.of();
        }

        List<String> output = new ArrayList<>();
        JSONArray data = o.getJSONArray(key);

        for (int i = 0; i < data.length(); i++) {
            output.add(data.getString(i));
        }

        return List.copyOf(output);
    }

    static List<Integer> ints(JSONObject o, String key) {
        if (o.isNull(key)) {
            return List.of();
        }

        List<Integer> output = new ArrayList<>();
        JSONArray data = o.getJSONArray(key);

        for (int i = 0; i < data.length(); i++) {
            output.add(data.getInt(i));
        }

        return List.copyOf(output);
    }

    static Integer nullableInt(JSONObject o, String key) {
        return o.isNull(key) ? null : o.getInt(key);
    }

    static Double nullableDouble(JSONObject o, String key) {
        return o.isNull(key) ? null : o.getDouble(key);
    }

    static String nullableString(JSONObject o, String key) {
        return o.isNull(key) ? null : o.getString(key);
    }

    static boolean bool(JSONObject o, String key) {
        return o.optBoolean(key, false);
    }
}
