package house.greenhouse.greenhouseconfig.jsonc;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

import java.util.Arrays;
import java.util.Objects;

public class JsonCElement implements CommentedValue {
	public static final JsonCElement EMPTY = new JsonCElement(JsonNull.INSTANCE);
	private final JsonElement json;
	private final String[] comments;

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

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;

		if (!(other instanceof JsonCElement element))
			return false;

		return json().equals(element.json()) && Arrays.equals(comments(), element.comments());
	}

	@Override
	public int hashCode() {
		return Objects.hash(json().hashCode(), Arrays.hashCode(comments()));
	}
}
