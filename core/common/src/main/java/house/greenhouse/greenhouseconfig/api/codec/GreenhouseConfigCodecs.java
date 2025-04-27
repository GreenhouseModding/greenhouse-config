package house.greenhouse.greenhouseconfig.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;
import house.greenhouse.greenhouseconfig.api.util.DefaultFieldUtil;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

@Deprecated(forRemoval = true, since = "2.0.0")
public class GreenhouseConfigCodecs {
    /**
	 * @see DefaultFieldUtil#codec(Codec, String, Object)
	 * @see DefaultFieldUtil#codecWithComments(Codec, String, Object, String...)
     */
    public static <A> MapCodec<A> defaultFieldCodec(final Codec<A> codec, final String name, final A defaultValue) {
        return DefaultFieldUtil.codec(codec, name, defaultValue);
    }

    /**
	 * @see house.greenhouse.greenhouseconfig.api.lang.CommentedValue#codec
	 * @see DefaultFieldUtil#codecWithComments(Codec, String, Object, String...)
     */
    public static <A> Codec<A> commentedCodec(Codec<A> codec, String... comments) {
		return CommentedValue.codec(codec, comments);
    }

    /**
	 * @see LateHolder#codec(ResourceKey)
	 */
    public static <E> Codec<LateHolder<E>> lateHolderCodec(ResourceKey<? extends Registry<E>> registry) {
		return LateHolder.codec(registry);
	}

    /**
	 * @see LateHolderSet#codec(ResourceKey)
     */
    public static <E> Codec<LateHolderSet<E>> lateHolderSetCodec(ResourceKey<? extends Registry<E>> registry) {
		return LateHolderSet.codec(registry);
	}
}
