package house.greenhouse.greenhouseconfig.impl.codec;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderSetImpl;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class LateHolderSetCodec<E> extends HolderSetCodec<E> {
    private final ResourceKey<? extends Registry<E>> registryKey;

    public LateHolderSetCodec(ResourceKey<? extends Registry<E>> registry) {
        super(registry, RegistryFixedCodec.create(registry), false);
        registryKey = registry;
    }

    @Override
    public <T> DataResult<Pair<HolderSet<E>, T>> decode(DynamicOps<T> ops, T value) {
        ImmutableList.Builder<TagKey<E>> tags = ImmutableList.builder();
        ImmutableList.Builder<ResourceKey<E>> entries = ImmutableList.builder();
        if (ops.getStream(value).isSuccess()) {
            ops.getStream(value).getOrThrow().filter(t -> ops.getStringValue(t).isSuccess()).forEach(t -> {
                String string = ops.getStringValue(t).getOrThrow();
                if (string.startsWith("#"))
                    tags.add(TagKey.create(registryKey, ResourceLocation.parse(string.substring(1))));
                else
                    entries.add(ResourceKey.create(registryKey, ResourceLocation.parse(string)));
            });
        } else if (ops.getStringValue(value).isSuccess()) {
            String string = ops.getStringValue(value).getOrThrow();
            if (string.startsWith("#"))
                tags.add(TagKey.create(registryKey, ResourceLocation.parse(string.substring(1))));
            else
                entries.add(ResourceKey.create(registryKey, ResourceLocation.parse(string)));
        }
        return DataResult.success(Pair.of(LateHolderSetImpl.createMixed((ResourceKey<Registry<E>>) registryKey, tags.build(), entries.build()), value));
    }

    @Override
    public <T> DataResult<T> encode(HolderSet<E> holderSet, DynamicOps<T> ops, T prefix) {
        if (holderSet instanceof LateHolderSetImpl<E> late)
            return DataResult.success(late.encode(ops, prefix));
        if (holderSet instanceof HolderSet.Named<E> named)
            return DataResult.success(ops.createString("#" + named.key().location()));
        return ResourceLocation.CODEC.listOf().encode(holderSet.stream().filter(e -> e.unwrapKey().isPresent())
                        .map(e -> e.unwrapKey().get().location()).toList(), ops, prefix);
    }
}
