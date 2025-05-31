package house.greenhouse.greenhouseconfig.jsonc;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

import java.util.*;

public class JsonCObject extends JsonCElement {
	public static final JsonCObject EMPTY = new JsonCObject(Map.of());
	private Map<String, JsonCElement> members = new LinkedHashMap<>();

	public JsonCObject(Map<String, JsonCElement> members) {
		super(null);
		this.members = members;
	}

	public JsonCObject(Map<String, JsonCElement> members, String... comments) {
		super(null, comments);
		this.members = members;
	}

	public JsonCObject(String... comments) {
		super(null, comments);
	}

	public JsonCObject(JsonObject object, String... comments) {
		super(null, comments);

		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			members.put(entry.getKey(), entry.getValue().isJsonObject() ?
					new JsonCObject(entry.getValue().getAsJsonObject()) :
					new JsonCElement(entry.getValue()));
		}
	}

	public Map<String, JsonCElement> toMap() {
		return ImmutableMap.copyOf(members);
	}

	public void put(String name, JsonCElement json) {
		members.put(name, json);
	}

	public void putAll(Map<String, JsonCElement> map) {
		members.putAll(map);
	}

	public void remove(String name) {
		members.remove(name);
	}

	public void clear() {
		members.clear();
	}

	@Override
	public CommentedValue withComment(String[] comments) {
		return new JsonCObject(members, comments);
	}
}
