package house.greenhouse.greenhouseconfig.toml.internal;

import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

public final class TomlEmpty extends TomlElement {
    public static final TomlEmpty INSTANCE = new TomlEmpty();
    
    private TomlEmpty() {
        super(new String[0]);
    }

    @Override
    public CommentedValue withComment(String[] comments) {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "TomlEmpty";
    }
}
