package house.greenhouse.greenhouseconfig.impl.util;

import com.mojang.datafixers.util.Either;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LateHolderImpl<T> implements LateHolder<T> {
    private final ResourceKey<Registry<T>> registry;
    private final ResourceKey<T> key;
    private Holder<T> value;

    public LateHolderImpl(ResourceKey<Registry<T>> registry, ResourceKey<T> key) {
        this.registry = registry;
        this.key = key;
    }

    @Override
    public void bind(HolderLookup.Provider registries, Consumer<String> onException) {
        if (registries.lookup(registryKey()).isEmpty())
            onException.accept("Could not find registry " + registryKey().location());
        HolderLookup.RegistryLookup<T> registry = registries.lookupOrThrow(registryKey());

        if (registry.get(key).isEmpty())
            onException.accept("Could not get value " + key.location() + " from registry " + key.registry() + ".");

        value = registry.getOrThrow(key);
    }

    @Override
    public void unbind() {
        value = null;
    }

    public String toString() {
        return "LateHolder[" + key + "]";
    }

    public ResourceKey<Registry<T>> registryKey() {
        return registry;
    }

    @Override
    public T value() {
        return value.value();
    }

    @Override
    public boolean isBound() {
        return value != null && value.isBound();
    }

    @Override
    public boolean is(ResourceLocation resourceLocation) {
        return value != null && value.is(resourceLocation);
    }

    @Override
    public boolean is(ResourceKey<T> resourceKey) {
        return value != null && value.is(resourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return value != null && value.is(predicate);
    }

    @Override
    public boolean is(TagKey<T> tagKey) {
        return value != null && value.is(tagKey);
    }

    @Override
    public boolean is(Holder<T> holder) {
        return value != null && value.is(holder);
    }

    @Override
    public Stream<TagKey<T>> tags() {
        if (value == null)
            return Stream.empty();
        return value.tags();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return Either.left(key);
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return true;
    }

    @Override
    public ResourceKey<T> key() {
        return key;
    }
}