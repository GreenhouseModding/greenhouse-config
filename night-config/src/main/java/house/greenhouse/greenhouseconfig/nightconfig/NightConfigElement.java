package house.greenhouse.greenhouseconfig.nightconfig;

import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

public abstract sealed class NightConfigElement implements CommentedValue permits NightConfigEmpty, NightConfigList,
    NightConfigObject, NightConfigValue {
    protected final String[] comments;

    public NightConfigElement(String[] comments) {
        this.comments = comments;
    }

    public String[] getComments() {
        return comments;
    }
}
