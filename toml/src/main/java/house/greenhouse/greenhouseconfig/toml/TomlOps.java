package house.greenhouse.greenhouseconfig.toml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import house.greenhouse.greenhouseconfig.toml.internal.TomlElement;
import house.greenhouse.greenhouseconfig.toml.internal.TomlEmpty;
import house.greenhouse.greenhouseconfig.toml.internal.TomlList;
import house.greenhouse.greenhouseconfig.toml.internal.TomlObject;
import house.greenhouse.greenhouseconfig.toml.internal.TomlValue;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

public final class TomlOps implements DynamicOps<TomlElement> {
    public static final TomlOps INSTANCE = new TomlOps();

    private TomlOps() {
    }

    @Override
    public TomlElement empty() {
        return TomlEmpty.INSTANCE;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, TomlElement input) {
        return null;
    }

    @Override
    public DataResult<Number> getNumberValue(TomlElement input) {
        if (input instanceof TomlValue value) {
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
    public TomlElement createNumeric(Number i) {
        return new TomlValue(i);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(TomlElement input) {
        if (input instanceof TomlValue value) {
            if (value.getValue() instanceof Boolean bool) {
                return DataResult.success(bool);
            } else {
                return DataResult.success(Boolean.parseBoolean(String.valueOf(value.getValue())));
            }
        }
        return DataResult.error(() -> "Cannot convert a non-value toml element into a boolean");
    }

    @Override
    public TomlElement createBoolean(boolean value) {
        return new TomlValue(value);
    }

    @Override
    public DataResult<String> getStringValue(TomlElement input) {
        if (input instanceof TomlValue value) {
            return DataResult.success(String.valueOf(value.getValue()));
        }
        return DataResult.error(() -> "Cannot convert a non-value toml element into a string");
    }

    @Override
    public TomlElement createString(String value) {
        return new TomlValue(value);
    }

    @Override
    public DataResult<TomlElement> mergeToList(TomlElement list, TomlElement value) {
        if (!(list instanceof TomlList) && list != empty())
            return DataResult.error(() -> "Cannot merge into a non-list toml element");

        TomlList newList = new TomlList(list.getComments());
        if (list instanceof TomlList tomlList) {
            newList.addAll(tomlList);
        }
        newList.add(value);
        return DataResult.success(newList);
    }

    @Override
    public DataResult<TomlElement> mergeToList(TomlElement list, List<TomlElement> values) {
        if (!(list instanceof TomlList) && list != empty())
            return DataResult.error(() -> "Cannot merge into a non-list toml element");

        TomlList newList = new TomlList(list.getComments());
        if (list instanceof TomlList tomlList) {
            newList.addAll(tomlList);
        }
        for (TomlElement value : values) {
            newList.add(value);
        }
        return DataResult.success(newList);
    }

    @Override
    public DataResult<TomlElement> mergeToMap(TomlElement map, TomlElement key, TomlElement value) {
        if (!(map instanceof TomlObject) && map != empty())
            return DataResult.error(() -> "Cannot merge into a non-map toml element");
        if (!(key instanceof TomlValue keyValue))
            return DataResult.error(() -> "Key is not a string or string-convertable");

        TomlObject newMap = new TomlObject(map.getComments());
        if (map instanceof TomlObject tomlObject) {
            newMap.putAll(tomlObject);
        }
        newMap.put(String.valueOf(keyValue.getValue()), value);
        return DataResult.success(newMap);
    }

    @Override
    public DataResult<TomlElement> mergeToMap(TomlElement map, Map<TomlElement, TomlElement> values) {
        if (!(map instanceof TomlObject) && map != empty())
            return DataResult.error(() -> "Cannot merge into a non-map toml element");

        List<TomlElement> missed = new ArrayList<>();
        TomlObject newMap = new TomlObject(map.getComments());
        if (map instanceof TomlObject tomlObject) {
            newMap.putAll(tomlObject);
        }

        for (var entry : values.entrySet()) {
            if (entry.getKey() instanceof TomlValue keyValue) {
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
    public DataResult<TomlElement> mergeToMap(TomlElement map, MapLike<TomlElement> values) {
        if (!(map instanceof TomlObject) && map != empty())
            return DataResult.error(() -> "Cannot merge into a non-map toml element");

        List<TomlElement> missed = new ArrayList<>();
        TomlObject newMap = new TomlObject(map.getComments());
        if (map instanceof TomlObject tomlObject) {
            newMap.putAll(tomlObject);
        }

        values.entries().forEach(entry -> {
            if (entry.getFirst() instanceof TomlValue keyValue) {
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
    public DataResult<Stream<Pair<TomlElement, TomlElement>>> getMapValues(TomlElement input) {
        if (!(input instanceof TomlObject object)) return DataResult.error(() -> "Input is not a toml object");
        return DataResult.success(
            object.toElementMap().entrySet().stream().map(e -> Pair.of(createString(e.getKey()), e.getValue())));
    }

    @Override
    public DataResult<Consumer<BiConsumer<TomlElement, TomlElement>>> getMapEntries(TomlElement input) {
        if (!(input instanceof TomlObject object)) return DataResult.error(() -> "Input is not a toml object");
        return DataResult.success(c -> {
            for (var entry : object.toElementMap().entrySet()) {
                c.accept(createString(entry.getKey()), entry.getValue());
            }
        });
    }

    @Override
    public DataResult<MapLike<TomlElement>> getMap(TomlElement input) {
        if (!(input instanceof TomlObject object)) return DataResult.error(() -> "Input is not a toml object");
        final Map<String, TomlElement> map = object.toElementMap();
        return DataResult.success(new MapLike<TomlElement>() {
            @Override
            public @Nullable TomlElement get(TomlElement key) {
                return map.get(String.valueOf(((TomlValue) key).getValue()));
            }

            @Override
            public @Nullable TomlElement get(String key) {
                return map.get(key);
            }

            @Override
            public Stream<Pair<TomlElement, TomlElement>> entries() {
                return map.entrySet().stream().map(e -> Pair.of(createString(e.getKey()), e.getValue()));
            }
        });
    }

    @Override
    public TomlElement createMap(Stream<Pair<TomlElement, TomlElement>> map) {
        TomlObject object = new TomlObject();
        map.forEach(pair -> {
            if (pair.getFirst() instanceof TomlValue value) {
                object.put(String.valueOf(value.getValue()), pair.getSecond());
            }
        });
        return object;
    }

    @Override
    public DataResult<Stream<TomlElement>> getStream(TomlElement input) {
        if (!(input instanceof TomlList list)) return DataResult.error(() -> "Input is not a toml list");
        return DataResult.success(list.toElementList().stream());
    }

    @Override
    public TomlElement createList(Stream<TomlElement> input) {
        TomlList list = new TomlList();
        input.forEach(list::add);
        return list;
    }

    @Override
    public TomlElement remove(TomlElement input, String key) {
        if (input instanceof TomlObject object) {
            return object.without(key);
        }
        return input;
    }
}
