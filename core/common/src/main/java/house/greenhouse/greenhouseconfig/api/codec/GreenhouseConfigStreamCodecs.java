package house.greenhouse.greenhouseconfig.api.codec;

import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.codec.stream.LateHolderStreamCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

@Deprecated
public class GreenhouseConfigStreamCodecs {
    /**
	 * @see LateHolderSet#codec(ResourceKey)
     */
    public static <E> StreamCodec<ByteBuf, LateHolder<E>> lateHolderStreamCodec(final ResourceKey<? extends Registry<E>> registry) {
		return new LateHolderStreamCodec<>(registry).map(holder -> (LateHolder<E>) holder, Function.identity());
    }

    /**
	 * @see LateHolderSet#streamCodec(ResourceKey)
     */
    public static <E> StreamCodec<ByteBuf, LateHolderSet<E>> lateHolderSetStreamCodec(final ResourceKey<? extends Registry<E>> registry) {
		return LateHolderSet.streamCodec(registry);
	}
}
