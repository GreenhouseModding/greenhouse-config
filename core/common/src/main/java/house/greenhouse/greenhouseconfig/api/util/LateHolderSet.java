package house.greenhouse.greenhouseconfig.api.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderSetImpl;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.List;

public abstract class LateHolderSet<T> extends HolderSet.ListBacked<T> implements Late {
    public static <T> LateHolderSet<T> createFromTags(ResourceKey<Registry<T>> registry, List<TagKey<T>> tags) {
        return new LateHolderSetImpl(registry, tags.stream().map(Either::left).toList());
    }

    public static <T> LateHolderSet<T> createFromEntries(ResourceKey<Registry<T>> registry, List<ResourceKey<T>> entries) {
        return new LateHolderSetImpl(registry, entries.stream().map(Either::right).toList());
    }

    public static <T> LateHolderSet<T> createMixed(ResourceKey<Registry<T>> registry, List<TagKey<T>> tags, List<ResourceKey<T>> entries) {
        ImmutableList.Builder<Either<TagKey<T>, ResourceKey<T>>> builder = ImmutableList.builder();
        builder.addAll(tags.stream().map(Either::<TagKey<T>, ResourceKey<T>>left).toList());
        builder.addAll(entries.stream().map(Either::<TagKey<T>, ResourceKey<T>>right).toList());
        return new LateHolderSetImpl<>(registry, builder.build());
    }
}
