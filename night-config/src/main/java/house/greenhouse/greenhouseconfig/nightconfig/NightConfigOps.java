package house.greenhouse.greenhouseconfig.nightconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

public final class NightConfigOps implements DynamicOps<NightConfigElement> {
    public static final NightConfigOps INSTANCE = new NightConfigOps();

    private NightConfigOps() {
    }

    @Override
    public NightConfigElement empty() {
        return NightConfigEmpty.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, NightConfigElement input) {
        return null;
    }

    @Override
    public DataResult<Number> getNumberValue(NightConfigElement input) {
        if (input instanceof NightConfigValue value) {
            if (value.getValue() instanceof Number number) {
                return DataResult.success(number);
            } else if (value.getValue() instanceof String str) {
                try {
                    return DataResult.success(Integer.parseInt(str));
                } catch (NumberFormatException e) {
                    try {
                        return DataResult.success(Double.parseDouble(str));
                    } catch (NumberFormatException e2) {
                        return DataResult.error(() -> "Cannot parse \"" + str + "\" as a number");
                    }
                }
            }
        }
        return DataResult.error(() -> "Cannot convert a non-value toml element into a number");
    }

    @Override
    public NightConfigElement createNumeric(Number i) {
        return new NightConfigValue(i);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(NightConfigElement input) {
        if (input instanceof NightConfigValue value) {
            if (value.getValue() instanceof Boolean bool) {
                return DataResult.success(bool);
            } else {
                return DataResult.success(Boolean.parseBoolean(String.valueOf(value.getValue())));
            }
        }
        return DataResult.error(() -> "Cannot convert a non-value toml element into a boolean");
    }

    @Override
    public NightConfigElement createBoolean(boolean value) {
        return new NightConfigValue(value);
    }

    @Override
    public DataResult<String> getStringValue(NightConfigElement input) {
        if (input instanceof NightConfigValue value) {
            return DataResult.success(String.valueOf(value.getValue()));
        }
        return DataResult.error(() -> "Cannot convert a non-value toml element into a string");
    }

    @Override
    public NightConfigElement createString(String value) {
        return new NightConfigValue(value);
    }

    @Override
    public DataResult<NightConfigElement> mergeToList(NightConfigElement list, NightConfigElement value) {
        if (!(list instanceof NightConfigList) && list != empty())
            return DataResult.error(() -> "Cannot merge into a non-list toml element");

        NightConfigList newList = new NightConfigList(list.getComments());
        if (list instanceof NightConfigList tomlList) {
            newList.addAll(tomlList);
        }
        newList.add(value);
        return DataResult.success(newList);
    }

    @Override
    public DataResult<NightConfigElement> mergeToList(NightConfigElement list, List<NightConfigElement> values) {
        if (!(list instanceof NightConfigList) && list != empty())
            return DataResult.error(() -> "Cannot merge into a non-list toml element");

        NightConfigList newList = new NightConfigList(list.getComments());
        if (list instanceof NightConfigList tomlList) {
            newList.addAll(tomlList);
        }
        for (NightConfigElement value : values) {
            newList.add(value);
        }
        return DataResult.success(newList);
    }

    @Override
    public DataResult<NightConfigElement> mergeToMap(NightConfigElement map, NightConfigElement key, NightConfigElement value) {
        if (!(map instanceof NightConfigObject) && map != empty())
            return DataResult.error(() -> "Cannot merge into a non-map toml element");
        if (!(key instanceof NightConfigValue keyValue))
            return DataResult.error(() -> "Key is not a string or string-convertable");

        NightConfigObject newMap = new NightConfigObject(map.getComments());
        if (map instanceof NightConfigObject tomlObject) {
            newMap.putAll(tomlObject);
        }
        newMap.put(String.valueOf(keyValue.getValue()), value);
        return DataResult.success(newMap);
    }

    @Override
    public DataResult<NightConfigElement> mergeToMap(NightConfigElement map, Map<NightConfigElement, NightConfigElement> values) {
        if (!(map instanceof NightConfigObject) && map != empty())
            return DataResult.error(() -> "Cannot merge into a non-map toml element");

        List<NightConfigElement> missed = new ArrayList<>();
        NightConfigObject newMap = new NightConfigObject(map.getComments());
        if (map instanceof NightConfigObject tomlObject) {
            newMap.putAll(tomlObject);
        }

        for (var entry : values.entrySet()) {
            if (entry.getKey() instanceof NightConfigValue keyValue) {
                newMap.put(String.valueOf(keyValue.getValue()), entry.getValue());
            } else {
                missed.add(entry.getKey());
            }
        }

        if (!missed.isEmpty()) {
            return DataResult.error(() -> "Some keys are not strings: " + missed, newMap);
        }

        return DataResult.success(newMap);
    }

    @Override
    public DataResult<NightConfigElement> mergeToMap(NightConfigElement map, MapLike<NightConfigElement> values) {
        if (!(map instanceof NightConfigObject) && map != empty())
            return DataResult.error(() -> "Cannot merge into a non-map toml element");

        List<NightConfigElement> missed = new ArrayList<>();
        NightConfigObject newMap = new NightConfigObject(map.getComments());
        if (map instanceof NightConfigObject tomlObject) {
            newMap.putAll(tomlObject);
        }

        values.entries().forEach(entry -> {
            if (entry.getFirst() instanceof NightConfigValue keyValue) {
                newMap.put(String.valueOf(keyValue.getValue()), entry.getSecond());
            } else {
                missed.add(entry.getFirst());
            }
        });

        if (!missed.isEmpty()) {
            return DataResult.error(() -> "Some keys are not strings: " + missed, newMap);
        }

        return DataResult.success(newMap);
    }

    @Override
    public DataResult<Stream<Pair<NightConfigElement, NightConfigElement>>> getMapValues(NightConfigElement input) {
        if (!(input instanceof NightConfigObject object)) return DataResult.error(() -> "Input is not a toml object");
        return DataResult.success(
            object.toElementMap().entrySet().stream().map(e -> Pair.of(createString(e.getKey()), e.getValue())));
    }

    @Override
    public DataResult<Consumer<BiConsumer<NightConfigElement, NightConfigElement>>> getMapEntries(
        NightConfigElement input) {
        if (!(input instanceof NightConfigObject object)) return DataResult.error(() -> "Input is not a toml object");
        return DataResult.success(c -> {
            for (var entry : object.toElementMap().entrySet()) {
                c.accept(createString(entry.getKey()), entry.getValue());
            }
        });
    }

    @Override
    public DataResult<MapLike<NightConfigElement>> getMap(NightConfigElement input) {
        if (!(input instanceof NightConfigObject object)) return DataResult.error(() -> "Input is not a toml object");
        final Map<String, NightConfigElement> map = object.toElementMap();
        return DataResult.success(new MapLike<NightConfigElement>() {
            @Override
            public @Nullable NightConfigElement get(NightConfigElement key) {
                return map.get(String.valueOf(((NightConfigValue) key).getValue()));
            }

            @Override
            public @Nullable NightConfigElement get(String key) {
                return map.get(key);
            }

            @Override
            public Stream<Pair<NightConfigElement, NightConfigElement>> entries() {
                return map.entrySet().stream().map(e -> Pair.of(createString(e.getKey()), e.getValue()));
            }
        });
    }

    @Override
    public NightConfigElement createMap(Stream<Pair<NightConfigElement, NightConfigElement>> map) {
        NightConfigObject object = new NightConfigObject();
        map.forEach(pair -> {
            if (pair.getFirst() instanceof NightConfigValue value) {
                object.put(String.valueOf(value.getValue()), pair.getSecond());
            }
        });
        return object;
    }

    @Override
    public DataResult<Stream<NightConfigElement>> getStream(NightConfigElement input) {
        if (!(input instanceof NightConfigList list)) return DataResult.error(() -> "Input is not a toml list");
        return DataResult.success(list.toElementList().stream());
    }

    @Override
    public NightConfigElement createList(Stream<NightConfigElement> input) {
        NightConfigList list = new NightConfigList();
        input.forEach(list::add);
        return list;
    }

    @Override
    public NightConfigElement remove(NightConfigElement input, String key) {
        if (input instanceof NightConfigObject object) {
            return object.without(key);
        }
        return input;
    }
}
