package house.greenhouse.greenhouseconfig.impl.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class LateHolderSetImpl<T> extends LateHolderSet<T> {
    private final ResourceKey<Registry<T>> registry;
    private final List<Either<TagKey<T>, ResourceKey<T>>> keys;
    private List<Holder<T>> contents;


    public LateHolderSetImpl(ResourceKey<Registry<T>> registry, List<Either<TagKey<T>, ResourceKey<T>>> keys) {
        this.registry = registry;
        this.keys = List.copyOf(keys);
    }

    @Override
    public void bind(HolderLookup.Provider registries, Consumer<String> onException) {
        if (registries.lookup(registryKey()).isEmpty())
            onException.accept("Could not find registry " + registryKey().location());
        HolderLookup.RegistryLookup<T> registry = registries.lookupOrThrow(registryKey());

        ImmutableList.Builder<Holder<T>> builder = ImmutableList.builder();
        keys.forEach(key -> {
            key.ifLeft(tagKey -> {
                if (registry.get(tagKey).isEmpty()) {
                        onException.accept("Could not get tag " + tagKey.location() + " from registry " + tagKey.registry().location() + ".");
                    return;
                }
                builder.addAll(registry.getOrThrow(tagKey).stream().toList());
            }).ifRight(resourceKey -> {
                if (registry.get(resourceKey).isEmpty()) {
                        onException.accept("Could not get " + resourceKey.location() + " from registry " + resourceKey.registry() + ".");
                    return;
                }
                builder.add(registry.getOrThrow(resourceKey));
            });
        });
        contents = builder.build();
    }

    @Override
    public void unbind() {
        contents = null;
    }

    public String toString() {
        return "LateHolderSet[" + keys + "]";
    }

    public <E> E encode(DynamicOps<E> ops, E prefix) {
        if (keys.size() == 1) {
            return ops.createString(keys.get(0).map(
                    tagKey -> "#" + tagKey.location(),
                    resourceKey -> resourceKey.location().toString())
            );
        }

        ListBuilder<E> builder = ops.listBuilder();
        for (Either<TagKey<T>, ResourceKey<T>> key : keys) {
            key
                    .ifLeft(tagKey -> builder.add(ops.createString("#" + tagKey.location())))
                    .ifRight(resourceKey -> builder.add(ops.createString(resourceKey.location().toString())));
        }
        return builder.build(prefix).getOrThrow();
    }

    @Override
    protected List<Holder<T>> contents() {
        if (contents != null)
            return contents;
        return List.of();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        if (contents != null)
            return Either.right(contents);
        return Either.right(List.of());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        if (contents != null)
            return contents.contains(holder);
        ResourceKey<T> holderKey = holder.unwrapKey().orElse(null);
        if (holderKey == null)
            return false;
        return keys.stream().anyMatch(either -> either.map(holder::is, resourceKey -> resourceKey.isFor(holderKey.registryKey()) && resourceKey.location().equals(holderKey.location())));
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    public ResourceKey<Registry<T>> registryKey() {
        return registry;
    }

    public List<Either<TagKey<T>, ResourceKey<T>>> keys() {
        return keys;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return true;
    }
}