package house.greenhouse.greenhouseconfig.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class LateHolderCodec<E> implements Codec<LateHolder<E>> {
	private final ResourceKey<? extends Registry<E>> registryKey;

	public LateHolderCodec(ResourceKey<? extends Registry<E>> registry) {
		registryKey = registry;
	}

	@Override
	public <T> DataResult<Pair<LateHolder<E>, T>> decode(DynamicOps<T> ops, T value) {
		String string = ops.getStringValue(value).getOrThrow();
		ResourceKey<E> key = ResourceKey.create(registryKey, ResourceLocation.parse(string));
		return DataResult.success(Pair.of(LateHolder.create(key), value));
	}

	@Override
	public <T> DataResult<T> encode(LateHolder<E> input, DynamicOps<T> ops, T prefix) {
		return ResourceLocation.CODEC.encode(input.key().location(), ops, prefix);
	}
}
