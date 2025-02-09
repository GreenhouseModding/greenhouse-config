package house.greenhouse.greenhouseconfig.toml.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.CommentedConfig;

public final class TomlObject extends TomlElement {
    private final CommentedConfig config;

    public TomlObject() {
        super(new String[0]);
        config = CommentedConfig.inMemory();
    }

    public TomlObject(String... comments) {
        super(comments);
        config = CommentedConfig.inMemory();
    }

    public TomlObject(CommentedConfig config) {
        super(new String[0]);
        this.config = config;
    }

    public TomlObject(CommentedConfig config, String... comments) {
        super(comments);
        this.config = config;
    }

    @Override
    public TomlObject withComment(String[] comments) {
        return new TomlObject(CommentedConfig.copy(config), comments);
    }

    public CommentedConfig getConfig() {
        return CommentedConfig.copy(config);
    }

    public void put(String name, TomlElement element) {
        switch (element) {
            case TomlList tomlList -> config.set(name, tomlList.getList());
            case TomlObject tomlObject -> config.set(name, tomlObject.getConfig());
            case TomlValue tomlValue -> config.set(name, tomlValue.getValue());
            case TomlEmpty ignored -> {
            }
        }

        config.setComment(name, String.join("\n", element.getComments()));
    }

    public void putAll(TomlObject object) {
        config.putAll(object.getConfig());
    }

    public Map<String, TomlElement> toElementMap() {
        Map<String, TomlElement> elementMap = new LinkedHashMap<>();
        for (var entry : config.entrySet()) {
            String[] comments = entry.getComment().split("\n");
            elementMap.put(entry.getKey(), switch (entry.getValue()) {
                case CommentedConfig c -> new TomlObject(CommentedConfig.copy(c), comments);
                case List<?> list -> new TomlList(new ArrayList<>(list), comments);
                default -> new TomlValue(entry.getValue(), comments);
            });
        }
        return elementMap;
    }
    
    public TomlObject without(String key) {
        CommentedConfig newConfig = CommentedConfig.inMemory();
        for (var entry : config.entrySet()) {
            if (!entry.getKey().equals(key)) {
                newConfig.set(entry.getKey(), entry.getValue());
                newConfig.setComment(entry.getKey(), entry.getComment());
            }
        }
        return new TomlObject(newConfig, comments);
    }

    @Override
    public String toString() {
        return "TomlObject{" +
            "config=" + config +
            ", comments=" + Arrays.toString(comments) +
            '}';
    }
}
