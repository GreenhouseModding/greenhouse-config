package house.greenhouse.greenhouseconfig.api.util;

import com.mojang.serialization.Codec;
import house.greenhouse.greenhouseconfig.impl.codec.LateHolderCodec;
import house.greenhouse.greenhouseconfig.impl.codec.stream.LateHolderStreamCodec;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link net.minecraft.core.Holder} that gets bound when registries are loaded.
 * @param <T> The type of objects inside this HolderSet.
 *
 * @see house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder.Builder#lateValues(Function, Consumer)
 */
public interface LateHolder<T> extends Holder<T>, Late {
	/**
	 * A {@link Codec} for a LateHolder.
	 *
	 * @param registry The registry key associated with the type of object within this LateHolder.
	 * @return A {@link LateHolder} codec.
	 * @param <T> The type of objects within the LateHolder.
	 */
	static <T> Codec<LateHolder<T>> codec(ResourceKey<? extends Registry<T>> registry) {
		return new LateHolderCodec<>(registry);
	}

	/**
	 * A {@link StreamCodec} for a LateHolder.
	 *
	 * @param registry The registry key associated with the type of object within this LateHolder.
	 * @return A {@link LateHolder} stream/packet codec.
	 * @param <T> The type of objects within the LateHolder.
	 */
	static <T> StreamCodec<ByteBuf, LateHolder<T>> streamCodec(ResourceKey<? extends Registry<T>> registry) {
		return new LateHolderStreamCodec<>(registry).map(holder -> (LateHolder<T>) holder, Function.identity());
	}

	/**
	 * Creates a LateHolder.
	 *
	 * @param key The key pointing towards the target resource for this holder.
	 * @return A LateHolder pointing towards a specific resource.
	 * @param <T> The type of the object within this LateHolder.
	 */
    static <T> LateHolder<T> create(ResourceKey<T> key) {
        return new LateHolderImpl<>(key.registryKey(), key);
    }

	/**
	 * The resource key of the object inside this holder.
	 */
    ResourceKey<T> key();
}
