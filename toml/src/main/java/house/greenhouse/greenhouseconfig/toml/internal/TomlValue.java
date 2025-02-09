package house.greenhouse.greenhouseconfig.toml.internal;

import java.util.Arrays;

public final class TomlValue extends TomlElement {
    private final Object value;

    public TomlValue(Object value) {
        super(new String[0]);
        this.value = value;
    }

    public TomlValue(Object value, String... comments) {
        super(comments);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public TomlValue withComment(String[] comments) {
        return new TomlValue(value, comments);
    }

    @Override
    public String toString() {
        return "TomlValue{" +
            "value=" + value +
            ", comments=" + Arrays.toString(comments) +
            '}';
    }
}
