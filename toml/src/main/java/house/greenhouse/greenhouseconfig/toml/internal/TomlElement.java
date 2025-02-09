package house.greenhouse.greenhouseconfig.toml.internal;

import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

public abstract sealed class TomlElement implements CommentedValue permits TomlEmpty, TomlList, TomlObject, TomlValue {
    protected final String[] comments;

    public TomlElement(String[] comments) {
        this.comments = comments;
    }

    public String[] getComments() {
        return comments;
    }
}
