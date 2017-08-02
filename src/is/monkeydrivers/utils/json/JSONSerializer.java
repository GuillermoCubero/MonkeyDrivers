package is.monkeydrivers.utils.json;

import static java.lang.Math.min;
import static java.lang.String.*;

public class JSONSerializer {

    private final String json;

    public JSONSerializer(String[] fields, String[] values) {
        String json = "";
        for (int i = 0; i < min(fields.length, values.length); i++)
            json += format("\"%s\":\"%s\",", fields[i], values[i]);

        this.json = "{" + (json.endsWith(",") ? json.substring(0, json.length() - 1) : json) + "}";
    }

    public String json() {
        return json;
    }

}
