package house.greenhouse.greenhouseconfig.jsonc;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import house.greenhouse.greenhouseconfig.jsonc.internal.JsonCElement;
import house.greenhouse.greenhouseconfig.jsonc.internal.JsonCObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class JsonCOps implements DynamicOps<JsonCElement> {
    public static final JsonCOps INSTANCE = new JsonCOps();

    @Override
    public String toString() {
        return "JSONC";
    }

    @Override
    public JsonCElement empty() {
        return JsonCElement.EMPTY;
    }

    @Override
    public JsonCObject emptyMap() {
        return JsonCObject.EMPTY;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, JsonCElement input) {
        Map<U, U> map = new HashMap<>();
        if (input instanceof JsonCObject object) {
            for (Map.Entry<String, JsonCElement> entry : object.members().entrySet())
                map.put(outOps.createString(entry.getKey()), JsonOps.INSTANCE.convertTo(outOps, input.json()));
            return outOps.createMap(map);
        }
        return JsonOps.INSTANCE.convertTo(outOps, input.json());
    }

    @Override
    public DataResult<Number> getNumberValue(JsonCElement input) {
        if (input instanceof JsonCObject)
            return DataResult.error(() -> "Cannot explicitly get number value from commented json object.");
        return JsonOps.INSTANCE.getNumberValue(input.json());
    }

    @Override
    public JsonCElement createNumeric(Number i) {
        return new JsonCElement(new JsonPrimitive(i));
    }

    @Override
    public DataResult<String> getStringValue(JsonCElement input) {
        if (input instanceof JsonCObject)
            return DataResult.error(() -> "Cannot explicitly get number value from commented json object.");
        return JsonOps.INSTANCE.getStringValue(input.json());
    }

    @Override
    public JsonCElement createString(String value) {
        return new JsonCElement(new JsonPrimitive(value));
    }

    @Override
    public DataResult<Boolean> getBooleanValue(JsonCElement input) {
        if (input instanceof JsonCObject)
            return DataResult.error(() -> "Cannot explicitly get number value from commented json object.");
        return JsonOps.INSTANCE.getBooleanValue(input.json());
    }

    @Override
    public JsonCElement createBoolean(final boolean value) {
        return new JsonCElement(new JsonPrimitive(value));
    }

    @Override
    public DataResult<JsonCElement> mergeToList(final JsonCElement list, final JsonCElement value) {
        if (list == empty())
            return DataResult.success(empty());

        if (!(list.json() instanceof JsonArray) && list != empty())
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);

        final JsonArray result = new JsonArray();
        if (list.json() != JsonNull.INSTANCE)
            result.addAll((JsonArray)list.json());

        result.add(value.json());
        return DataResult.success(new JsonCElement(result, list.comments()));
    }

    @Override
    public DataResult<JsonCElement> mergeToList(final JsonCElement list, final List<JsonCElement> values) {
        if (list == null)
            return DataResult.error(() -> "mergeToList called with null.");

        if (!(list instanceof JsonCElement) && list != empty())
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);

        final JsonArray result = new JsonArray();
        if (list != empty())
            result.addAll((JsonArray)list.json());

        values.forEach(commented -> result.add(commented.json()));
        return DataResult.success(new JsonCElement(result, list.comments()));
    }

    @Override
    public DataResult<JsonCElement> mergeToMap(JsonCElement map, JsonCElement key, JsonCElement value) {
        if (!(map instanceof JsonCObject) && map != empty())
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);

        if (!(key.json() instanceof JsonPrimitive || !key.json().getAsJsonPrimitive().isString()))
            return DataResult.error(() -> "key is not a string: " + key, map);

        final Map<String, JsonCElement> output = new HashMap<>();
        if (map != null && map != empty())
            output.putAll(((JsonCObject) map).members());
        output.put(key.json().getAsString(), value);

        return DataResult.success(new JsonCObject(output, map != null ? map.comments() : new String[]{}));
    }

    @Override
    public DataResult<JsonCElement> mergeToMap(final JsonCElement map, final MapLike<JsonCElement> values) {
        if (!(map instanceof JsonCObject) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        final JsonCObject output = new JsonCObject(map != null ? map.comments() : new String[]{});
        if (map != null && map != empty())
            output.putAll(((JsonCObject) map).members());

        final List<JsonElement> missed = Lists.newArrayList();

        values.entries().forEach(entry -> {
            final JsonElement key = entry.getFirst().json();
            if (!(key instanceof JsonPrimitive) || !key.getAsJsonPrimitive().isString()) {
                missed.add(key);
                return;
            }
            output.put(key.getAsString(), entry.getSecond());
        });

        if (!missed.isEmpty())
            return DataResult.error(() -> "some keys are not strings: " + missed, output);

        return DataResult.success(output);
    }
    @Override
    public DataResult<Stream<Pair<JsonCElement, JsonCElement>>> getMapValues(final JsonCElement input) {
        if (!(input instanceof JsonCObject object))
            return DataResult.error(() -> "Not a JSON object: " + input);

        return DataResult.success(object.members().entrySet().stream().map(entry -> Pair.of(createString(entry.getKey()), entry.getValue())));
    }

    @Override
    public DataResult<Consumer<BiConsumer<JsonCElement, JsonCElement>>> getMapEntries(final JsonCElement input) {
        if (!(input instanceof JsonCObject object))
            return DataResult.error(() -> "Not a JSON object: " + input);

        return DataResult.success(c -> {
            for (final Map.Entry<String, JsonCElement> entry : object.members().entrySet()) {
                c.accept(createString(entry.getKey()), entry.getValue());
            }
        });
    }

    @Override
    public DataResult<MapLike<JsonCElement>> getMap(final JsonCElement input) {
        if (!(input instanceof JsonCObject object)) {
            return DataResult.error(() -> "Not a commented JSON object: " + input);
        }
        return DataResult.success(new MapLike<>() {
            @Nullable
            @Override
            public JsonCElement get(final JsonCElement key) {
                if (key.json() instanceof JsonPrimitive primitive && primitive.isString()) {
                    final JsonCElement element = object.members().get((primitive.getAsString()));
                    if (element.json() instanceof JsonNull)
                        return null;
                    return element;
                }
                throw new IllegalArgumentException("Could not get JsonElement from CommentedJson.Object from a non string primitive.");
            }

            @Nullable
            @Override
            public JsonCElement get(final String key) {
                final JsonCElement element = object.members().get(key);
                if (element.json() instanceof JsonNull) {
                    return null;
                }
                return element;
            }

            @Override
            public Stream<Pair<JsonCElement, JsonCElement>> entries() {
                return object.members().entrySet().stream().map(e -> Pair.of(createString(e.getKey()), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + object + "]";
            }
        });
    }

    @Override
    public JsonCElement createMap(final Stream<Pair<JsonCElement, JsonCElement>> map) {
        final JsonCObject result = new JsonCObject();
        map.forEach(p -> result.put(p.getFirst().json().getAsString(), p.getSecond()));
        return result;
    }

    @Override
    public DataResult<Stream<JsonCElement>> getStream(final JsonCElement input) {
        if (input.json() instanceof JsonArray array) {
            return DataResult.success(array.asList().stream().map(e -> e instanceof JsonNull ? null : new JsonCElement(e)));
        }
        return DataResult.error(() -> "Not a json array: " + input);
    }

    @Override
    public DataResult<Consumer<Consumer<JsonCElement>>> getList(final JsonCElement input) {
        if (input.json() instanceof JsonArray array) {
            return DataResult.success(c -> {
                for (final JsonElement element : array.asList()) {
                    c.accept(element instanceof JsonNull ? null : new JsonCElement(element));
                }
            });
        }
        return DataResult.error(() -> "Not a jsonc array: " + input);
    }

    @Override
    public JsonCElement createList(final Stream<JsonCElement> input) {
        final JsonArray result = new JsonArray();
        input.forEach(commented -> result.add(commented.json()));
        return new JsonCElement(result);
    }

    @Override
    public JsonCElement remove(final JsonCElement input, final String key) {
        if (input.json() instanceof JsonObject object) {
            final JsonObject result = new JsonObject();
            object.entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), key)).forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            return new JsonCElement(result, input.comments());
        }
        return input;
    }

    @Override
    public ListBuilder<JsonCElement> listBuilder() {
        return new ArrayBuilder();
    }

    @Override
    public RecordBuilder<JsonCElement> mapBuilder() {
        return new JsonCRecordBuilder();
    }

    private static final class ArrayBuilder implements ListBuilder<JsonCElement> {
        private DataResult<JsonCElement> builder = DataResult.success(new JsonCElement(new JsonArray()), Lifecycle.stable());

        @Override
        public DynamicOps<JsonCElement> ops() {
            return INSTANCE;
        }

        @Override
        public ListBuilder<JsonCElement> add(final JsonCElement value) {
            builder = builder.map(b -> {
                ((JsonArray)b.json()).add(value.json());
                return b;
            });
            return this;
        }

        @Override
        public ListBuilder<JsonCElement> add(final DataResult<JsonCElement> value) {
            if (value.isError())
                return this;
            builder = builder.apply2stable((b, element) -> {
                ((JsonArray)b.json()).add(value.getOrThrow().json());
                return b;
            }, value);
            return this;
        }

        @Override
        public ListBuilder<JsonCElement> withErrorsFrom(final DataResult<?> result) {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<JsonCElement> mapError(final UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<JsonCElement> build(final JsonCElement prefix) {
            final DataResult<JsonCElement> result = builder.flatMap(b -> {
                if (prefix == ops().empty()) {
                    return DataResult.success(b, Lifecycle.stable());
                }

                if (!(prefix.json() instanceof JsonArray)) {
                    return DataResult.error(() -> "Cannot append a list to not a list: " + prefix, prefix);
                }

                final JsonArray array = new JsonArray();
                array.addAll((JsonArray) prefix.json());
                array.addAll((JsonArray) b.json());
                return DataResult.success(new JsonCElement(array), Lifecycle.stable());
            });

            builder = result;
            return result;
        }
    }

    private class JsonCRecordBuilder extends RecordBuilder.AbstractStringBuilder<JsonCElement, JsonCObject> {
        protected JsonCRecordBuilder() {
            super(JsonCOps.this);
        }

        @Override
        protected JsonCObject initBuilder() {
            return new JsonCObject();
        }


        @Override
        protected JsonCObject append(String key, JsonCElement value, JsonCObject builder) {
            builder.put(key, value);
            return builder;
        }

        @Override
        protected DataResult<JsonCElement> build(final JsonCObject builder, final JsonCElement prefix) {
            if (prefix == null || prefix == ops().empty()) {
                return DataResult.success(builder);
            }
            if (prefix instanceof JsonCObject object) {
                final JsonCObject result = new JsonCObject();
                for (final Map.Entry<String, JsonCElement> entry : object.members().entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
                for (final Map.Entry<String, JsonCElement> entry : builder.members().entrySet()) {
                    result.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success(result);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }
    }
}
