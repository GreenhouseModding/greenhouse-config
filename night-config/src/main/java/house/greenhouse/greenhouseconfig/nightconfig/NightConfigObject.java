package house.greenhouse.greenhouseconfig.nightconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;

public final class NightConfigObject extends NightConfigElement {
    private final CommentedConfig config;

    public NightConfigObject() {
        super(new String[0]);
        config = InMemoryCommentedFormat.defaultInstance().createConfig(LinkedHashMap::new);
    }

    public NightConfigObject(String... comments) {
        super(comments);
		config = InMemoryCommentedFormat.defaultInstance().createConfig(LinkedHashMap::new);
    }

    public NightConfigObject(CommentedConfig config) {
        super(new String[0]);
        this.config = config;
    }

    public NightConfigObject(CommentedConfig config, String... comments) {
        super(comments);
        this.config = config;
    }

    @Override
    public NightConfigObject withComment(String[] comments) {
        return new NightConfigObject(getConfig(), comments);
    }

    public CommentedConfig getConfig() {
        return CommentedConfig.copy(config, LinkedHashMap::new);
    }

    public void put(String name, NightConfigElement element) {
        switch (element) {
            case NightConfigList tomlList -> config.set(name, tomlList.getList());
            case NightConfigObject tomlObject -> config.set(name, tomlObject.getConfig());
            case NightConfigValue tomlValue -> config.set(name, tomlValue.getValue());
            case NightConfigEmpty ignored -> {
            }
        }

        config.setComment(name, String.join("\n", element.getComments()));
    }

    public void putAll(NightConfigObject object) {
        config.putAll(object.getConfig());
    }

    public Map<String, NightConfigElement> toMap() {
        Map<String, NightConfigElement> elementMap = new LinkedHashMap<>();
        for (var entry : config.entrySet()) {
            String comment = entry.getComment();
            String[] comments;
            if (comment != null) {
                comments = comment.split("\n");
            } else {
                comments = new String[0];
            }
            elementMap.put(entry.getKey(), switch (entry.getValue()) {
                case CommentedConfig c -> new NightConfigObject(CommentedConfig.copy(c, LinkedHashMap::new), comments);
                case List<?> list -> new NightConfigList(new ArrayList<>(list), comments);
                default -> new NightConfigValue(entry.getValue(), comments);
            });
        }
        return elementMap;
    }

    public NightConfigObject without(String key) {
        CommentedConfig newConfig = InMemoryCommentedFormat.defaultInstance().createConfig(LinkedHashMap::new);
        for (var entry : config.entrySet()) {
            if (!entry.getKey().equals(key)) {
                newConfig.set(entry.getKey(), entry.getValue());
                newConfig.setComment(entry.getKey(), entry.getComment());
            }
        }
        return new NightConfigObject(newConfig, comments);
    }

    @Override
    public String toString() {
        return "NightConfigObject{" +
            "config=" + config +
            ", comments=" + Arrays.toString(comments) +
            '}';
    }
}
