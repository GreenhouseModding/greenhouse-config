package house.greenhouse.greenhouseconfig.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderSetImpl;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

public class LateHolderSetCodec<E> extends HolderSetCodec<E> {
	private final ResourceKey<? extends Registry<E>> registryKey;

	public LateHolderSetCodec(ResourceKey<? extends Registry<E>> registry) {
		super(registry, RegistryFixedCodec.create(registry), false);
		registryKey = registry;
	}

	@Override
	public <T> @NotNull DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> ops, @NotNull T value) {
		LateHolderSet.Builder<E> builder = LateHolderSet.builder(registryKey);
		if (ops.getStream(value).isSuccess()) {
			ops.getStream(value).getOrThrow().filter(t -> ops.getStringValue(t).isSuccess()).forEach(t -> {
				String string = ops.getStringValue(t).getOrThrow();
				if (string.startsWith("#"))
					builder.add(TagKey.create(registryKey, ResourceLocation.parse(string.substring(1))));
				else
					builder.add(ResourceKey.create(registryKey, ResourceLocation.parse(string)));
			});
		} else if (ops.getStringValue(value).isSuccess()) {
			String string = ops.getStringValue(value).getOrThrow();
			if (string.startsWith("#"))
				builder.add(TagKey.create(registryKey, ResourceLocation.parse(string.substring(1))));
			else
				builder.add(ResourceKey.create(registryKey, ResourceLocation.parse(string)));
		}
		return DataResult.success(Pair.of(builder.build(), value));
	}

	@Override
	public <T> @NotNull DataResult<T> encode(@NotNull HolderSet<E> holderSet, @NotNull DynamicOps<T> ops, @NotNull T prefix) {
		if (holderSet instanceof LateHolderSetImpl<E> late)
			return DataResult.success(late.encode(ops, prefix));
		if (holderSet instanceof HolderSet.Named<E> named)
			return DataResult.success(ops.createString("#" + named.key().location()));
		return ResourceLocation.CODEC.listOf()
				.encode(holderSet.stream()
						.filter(e -> e.unwrapKey().isPresent())
						.map(e -> e.unwrapKey().orElseThrow().location()).toList(), ops, prefix);
	}
}
