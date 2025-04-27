package house.greenhouse.greenhouseconfig.nightconfig;

import java.util.Arrays;

public final class NightConfigValue extends NightConfigElement {
    private final Object value;

    public NightConfigValue(Object value) {
        super(new String[0]);
        this.value = value;
    }

    public NightConfigValue(Object value, String... comments) {
        super(comments);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public NightConfigValue withComment(String[] comments) {
        return new NightConfigValue(value, comments);
    }

    @Override
    public String toString() {
        return "NightConfigValue{" +
            "value=" + value +
            ", comments=" + Arrays.toString(comments) +
            '}';
    }
}
