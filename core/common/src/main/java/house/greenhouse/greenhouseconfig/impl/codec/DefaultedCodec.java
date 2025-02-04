package house.greenhouse.greenhouseconfig.impl.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.stream.Stream;

public class DefaultedCodec<A> extends MapCodec<A> {
    private final String name;
    private final Codec<A> elementCodec;
    private final A defaultValue;

    public DefaultedCodec(final String name, final Codec<A> elementCodec, final A defaultValue) {
        this.name = name;
        this.elementCodec = elementCodec;
        this.defaultValue = defaultValue;
    }

    @Override
    public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
        final T value = input.get(name);
        if (value == null)
            return DataResult.error(() -> "Field '" + name + "' is not present in '" + input + "'.");
        final DataResult<A> parsed = elementCodec.parse(ops, value);
        return parsed.setPartial(parsed.resultOrPartial().orElseThrow());
    }

    @Override
    public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        if (input == null)
            return prefix.add(name, elementCodec.encodeStart(ops, defaultValue));
        return prefix.add(name, elementCodec.encodeStart(ops, input));
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.of(ops.createString(name));
    }
}
