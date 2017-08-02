package is.monkeydrivers.utils.json;

import java.util.regex.Matcher;

import static java.util.regex.Pattern.*;

public class JSONDeserializer {
    private final String json;

    public JSONDeserializer(String json) {
        this.json = json;
    }

    public String getValueOfField(String field) {
        Matcher matcher = compile("\"" + field + "\":\"(\\w+)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : "null";
    }

}
