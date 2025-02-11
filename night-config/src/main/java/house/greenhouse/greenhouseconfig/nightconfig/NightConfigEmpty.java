package house.greenhouse.greenhouseconfig.nightconfig;

import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

public final class NightConfigEmpty extends NightConfigElement {
    public static final NightConfigEmpty INSTANCE = new NightConfigEmpty();
    
    private NightConfigEmpty() {
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
