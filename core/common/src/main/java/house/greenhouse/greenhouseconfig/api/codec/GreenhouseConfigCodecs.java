package house.greenhouse.greenhouseconfig.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.codec.LateHolderCodec;
import house.greenhouse.greenhouseconfig.impl.codec.DefaultedCodec;
import house.greenhouse.greenhouseconfig.impl.codec.LateHolderSetCodec;
import house.greenhouse.greenhouseconfig.impl.codec.CommentedCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Consumer;
import java.util.function.Function;

public class GreenhouseConfigCodecs {
    /**
     * A {@link MapCodec} that will encode as a specified default field if not specified.
     *
     * @param codec The codec to use for this object.
     * @param name The key of the field.
     * @param defaultValue The default value to encode.
     * @return A default field codec.
     * @param <A> The codec's type parameter.
     */
    public static <A> MapCodec<A> defaultFieldCodec(final Codec<A> codec, final String name, final A defaultValue) {
        return new DefaultedCodec<>(name, codec, defaultValue).setPartial(() -> defaultValue);
    }

    /**
     * A {@link Codec} that encodes comments alongside its value.
     *
     * @param codec A codec.
     * @param comments  The comments to encode. New values are a new line.
     * @return A commented codec.
     * @param <A> The codec's type parameter.
     */
    public static <A> Codec<A> commentedCodec(Codec<A> codec, String... comments) {
        return new CommentedCodec<>(codec, comments);
    }

    /**
     * A {@link Codec} for a LateHolder, a {@link net.minecraft.core.Holder} that gets bound when registries are loaded.
     *
     * @param registry The registry key to use for this codec.
     * @return A {@link LateHolder} codec.
     * @param <E> The type that the registry holds.
     * @see house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder.Builder#lateValues(Function, Consumer)
     */
    public static <E> Codec<LateHolder<E>> lateHolderCodec(ResourceKey<Registry<E>> registry) {
        return new LateHolderCodec<>(registry).xmap(holder -> (LateHolder<E>)holder, holder -> holder);
    }

    /**
     * A {@link Codec} for a LateHolderSet, a {@link net.minecraft.core.HolderSet} that gets bound when registries are loaded.
     *
     * @param registry The registry key to use for this codec.
     * @return A {@link LateHolderSet} codec.
     * @param <E> The type that the registry holds.
     * @see house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder.Builder#lateValues(Function, Consumer)
     */
    public static <E> Codec<LateHolderSet<E>> lateHolderSetCodec(ResourceKey<? extends Registry<E>> registry) {
        return new LateHolderSetCodec<>(registry).xmap(holder -> (LateHolderSet<E>)holder, holder -> holder);
    }
}