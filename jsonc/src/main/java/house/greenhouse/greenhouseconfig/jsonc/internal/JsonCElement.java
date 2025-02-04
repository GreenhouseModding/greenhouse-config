package house.greenhouse.greenhouseconfig.jsonc.internal;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;

import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

public class JsonCElement implements CommentedValue {
    private final JsonElement json;
    private final String[] comments;

    public static final JsonCElement EMPTY = new JsonCElement(JsonNull.INSTANCE);

    public JsonCElement(JsonElement json) {
        this.json = json;
        this.comments = new String[]{};
    }

    public JsonCElement(JsonElement json, String... comments) {
        this.json = json;
        this.comments = comments;
    }

    public JsonElement json() {
        return json;
    }

    public String[] comments() {
        return comments;
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return new JsonCElement(json, comments);
    }
}
