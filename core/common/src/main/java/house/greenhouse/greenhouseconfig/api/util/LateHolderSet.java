package house.greenhouse.greenhouseconfig.api.util;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import house.greenhouse.greenhouseconfig.impl.codec.LateHolderSetCodec;
import house.greenhouse.greenhouseconfig.impl.codec.stream.LateHolderSetStreamCodec;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderSetImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A {@link net.minecraft.core.HolderSet} that gets bound when registries are loaded, and can support loading tags and entries in the same set.
 *
 * @param <T> The type of objects inside this HolderSet.
 * @see house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder.Builder#lateValues(Function, Consumer)
 */
public abstract class LateHolderSet<T> extends HolderSet.ListBacked<T> implements Late {
	/**
	 * A {@link Codec} for a LateHolderSet.
	 *
	 * @param registry The registry key associated with the type of object within this LateHolderSet.
	 * @param <T>      The type of objects within the LateHolderSet.
	 * @return A {@link LateHolderSet} codec.
	 */
	public static <T> Codec<LateHolderSet<T>> codec(ResourceKey<? extends Registry<T>> registry) {
		return new LateHolderSetCodec<>(registry).xmap(holder -> (LateHolderSet<T>) holder, Function.identity());
	}

	/**
	 * A {@link StreamCodec} for a LateHolderSet.
	 *
	 * @param registry The registry key associated with the type of object within this LateHolderSet.
	 * @param <T>      The type of objects within the LateHolderSet.
	 * @return A {@link LateHolderSet} stream/packet codec.
	 * @see house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder.Builder#lateValues(Function, Consumer)
	 */
	public static <T> StreamCodec<ByteBuf, LateHolderSet<T>> streamCodec(final ResourceKey<? extends Registry<T>> registry) {
		return new LateHolderSetStreamCodec<>(registry).map(holders -> (LateHolderSet<T>) holders, Function.identity());
	}

	/**
	 * Creates a builder to build a LateHolderSet.
	 *
	 * @param registry The registry key associated with the type of object within this LateHolderSet.
	 * @param <T>      The type of objects within the LateHolderSet.
	 * @return A LateHolderSet builder.
	 */
	public static <T> Builder<T> builder(final ResourceKey<? extends Registry<T>> registry) {
		return new Builder<>(registry);
	}

	public static class Builder<T> {
		private final ResourceKey<? extends Registry<T>> registry;
		private final ImmutableList.Builder<Either<TagKey<T>, ResourceKey<T>>> listBuilder = ImmutableList.builder();

		public Builder(ResourceKey<? extends Registry<T>> registry) {
			this.registry = registry;
		}

		@SafeVarargs
		public final Builder<T> add(ResourceKey<T>... keys) {
			listBuilder.addAll(Arrays.stream(keys).map(Either::<TagKey<T>, ResourceKey<T>>right).toList());
			return this;
		}

		@SafeVarargs
		public final Builder<T> add(TagKey<T>... tags) {
			listBuilder.addAll(Arrays.stream(tags).map(Either::<TagKey<T>, ResourceKey<T>>left).toList());
			return this;
		}

		public LateHolderSet<T> build() {
			return new LateHolderSetImpl<>(registry, listBuilder.build());
		}
	}

}
