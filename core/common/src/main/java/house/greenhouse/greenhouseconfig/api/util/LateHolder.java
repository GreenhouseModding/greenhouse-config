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

import java.util.function.Function;

public interface LateHolder<T> extends Holder<T>, Late {
	static <T> Codec<LateHolder<T>> codec(ResourceKey<? extends Registry<T>> registry) {
		return new LateHolderCodec<>(registry);
	}

	static <T> StreamCodec<ByteBuf, LateHolder<T>> streamCodec(ResourceKey<? extends Registry<T>> registry) {
		return new LateHolderStreamCodec<>(registry).map(holder -> (LateHolder<T>) holder, Function.identity());
	}

    static <T> LateHolder<T> create(ResourceKey<T> key) {
        return new LateHolderImpl<>(key.registryKey(), key);
    }

    ResourceKey<T> key();
}
