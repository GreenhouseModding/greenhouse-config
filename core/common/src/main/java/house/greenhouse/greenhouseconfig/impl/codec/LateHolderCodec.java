package house.greenhouse.greenhouseconfig.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class LateHolderCodec<E> implements Codec<Holder<E>> {
    private final ResourceKey<? extends Registry<E>> registryKey;
    private final Codec<Holder<E>> registryFixedCodec;

    public LateHolderCodec(ResourceKey<? extends Registry<E>> registry) {
        registryKey = registry;
        registryFixedCodec = RegistryFixedCodec.create(registry);
    }

    @Override
    public <T> DataResult<Pair<Holder<E>, T>> decode(DynamicOps<T> ops, T value) {
        String string = ops.getStringValue(value).getOrThrow();
        ResourceKey<E> key = ResourceKey.create(registryKey, ResourceLocation.parse(string));
        return DataResult.success(Pair.of(LateHolder.create(key), value));
    }

    @Override
    public <T> DataResult<T> encode(Holder<E> input, DynamicOps<T> ops, T prefix) {
        if (!(input instanceof LateHolder<E> lateHolder))
            return registryFixedCodec.encode(input, ops, prefix);
        return ResourceLocation.CODEC.encode(lateHolder.key().location(), ops, prefix);
    }
}
