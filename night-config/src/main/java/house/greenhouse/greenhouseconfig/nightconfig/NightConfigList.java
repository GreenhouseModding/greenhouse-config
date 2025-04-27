package house.greenhouse.greenhouseconfig.nightconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.CommentedConfig;

public final class NightConfigList extends NightConfigElement {
    private final List<Object> list;

    public NightConfigList() {
        super(new String[0]);
        list = new ArrayList<>();
    }

    public NightConfigList(String... comments) {
        super(comments);
        list = new ArrayList<>();
    }

    public NightConfigList(List<Object> list) {
        super(new String[0]);
        this.list = list;
    }

    public NightConfigList(List<Object> list, String... comments) {
        super(comments);
        this.list = list;
    }

    @Override
    public NightConfigList withComment(String[] comments) {
        return new NightConfigList(new ArrayList<>(list), comments);
    }

    public List<?> getList() {
        return new ArrayList<>(list);
    }

    public void add(NightConfigElement element) {
        switch (element) {
            case NightConfigList tomlList -> list.add(tomlList.getList());
            case NightConfigObject tomlObject -> list.add(tomlObject.getConfig());
            case NightConfigValue tomlValue -> list.add(tomlValue.getValue());
            case NightConfigEmpty ignored -> {}
        }
    }

    public void addAll(NightConfigList tomlList) {
        list.addAll(tomlList.getList());
    }

    public List<NightConfigElement> toElementList() {
        List<NightConfigElement> elementList = new ArrayList<>();
        for (Object o : list) {
            elementList.add(switch (o) {
                case CommentedConfig config -> new NightConfigObject(config);
                case List<?> l -> new NightConfigList(new ArrayList<>(l));
                default -> new NightConfigValue(o);
            });
        }
        return elementList;
    }

    @Override
    public String toString() {
        return "NightConfigList{" +
            "list=" + list +
            ", comments=" + Arrays.toString(comments) +
            '}';
    }
}
