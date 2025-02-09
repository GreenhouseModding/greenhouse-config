package house.greenhouse.greenhouseconfig.toml.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.CommentedConfig;

public final class TomlList extends TomlElement {
    private final List<Object> list;

    public TomlList() {
        super(new String[0]);
        list = new ArrayList<>();
    }

    public TomlList(String... comments) {
        super(comments);
        list = new ArrayList<>();
    }

    public TomlList(List<Object> list) {
        super(new String[0]);
        this.list = list;
    }

    public TomlList(List<Object> list, String... comments) {
        super(comments);
        this.list = list;
    }

    @Override
    public TomlList withComment(String[] comments) {
        return new TomlList(new ArrayList<>(list), comments);
    }

    public List<?> getList() {
        return new ArrayList<>(list);
    }

    public void add(TomlElement element) {
        switch (element) {
            case TomlList tomlList -> list.add(tomlList.getList());
            case TomlObject tomlObject -> list.add(tomlObject.getConfig());
            case TomlValue tomlValue -> list.add(tomlValue.getValue());
            case TomlEmpty ignored -> {}
        }
    }
    
    public void addAll(TomlList tomlList) {
        list.addAll(tomlList.getList());
    }
    
    public List<TomlElement> toElementList() {
        List<TomlElement> elementList = new ArrayList<>();
        for (Object o : list) {
            elementList.add(switch (o) {
                case CommentedConfig config -> new TomlObject(config);
                case List<?> l -> new TomlList(new ArrayList<>(l));
                default -> new TomlValue(o);
            });
        }
        return elementList;
    }

    @Override
    public String toString() {
        return "TomlList{" +
            "list=" + list +
            ", comments=" + Arrays.toString(comments) +
            '}';
    }
}
