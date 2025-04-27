package house.greenhouse.greenhouseconfig.impl.codec.stream;

import com.mojang.datafixers.util.Either;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderSetImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LateHolderSetStreamCodec<T> implements StreamCodec<ByteBuf, HolderSet<T>> {
	private final ResourceKey<? extends Registry<T>> registry;
	private final StreamCodec<ByteBuf, ResourceKey<T>> holderCodec;

	public LateHolderSetStreamCodec(ResourceKey<? extends Registry<T>> registry) {
		this.registry = registry;
		holderCodec = ResourceKey.streamCodec(registry);
	}

	@Override
	public @NotNull HolderSet<T> decode(@NotNull ByteBuf buf) {
		int i = VarInt.read(buf) - 1;
		LateHolderSet.Builder<T> builder = LateHolderSet.builder(registry);
		if (i == -1) {
			builder.add(TagKey.create(registry, ResourceLocation.STREAM_CODEC.decode(buf)));
		} else {
			for (int j = 0; j < i; ++j) {
				boolean isTag = buf.readBoolean();
				if (isTag)
					builder.add(TagKey.create(registry, ResourceLocation.STREAM_CODEC.decode(buf)));
				else
					builder.add(holderCodec.decode(buf));
			}
		}
		return builder.build();
	}

	@Override
	public void encode(@NotNull ByteBuf buf, @NotNull HolderSet<T> holderSet) {
		Optional<TagKey<T>> optional = holderSet.unwrapKey();
		if (optional.isPresent()) {
			VarInt.write(buf, -1);
			ResourceLocation.STREAM_CODEC.encode(buf, optional.get().location());
		} else {
			VarInt.write(buf, holderSet.size() + 1);
			if (holderSet instanceof LateHolderSetImpl<T> late) {

				for (Either<TagKey<T>, ResourceKey<T>> value : late.keys()) {
					if (value.left().isPresent()) {
						buf.writeBoolean(true);
						ResourceLocation.STREAM_CODEC.encode(buf, value.left().orElseThrow().location());
					} else {
						buf.writeBoolean(false);
						holderCodec.encode(buf, value.right().orElseThrow());
					}
				}
			} else {
				for (Holder<T> value : holderSet) {
					buf.writeBoolean(false);
					holderCodec.encode(buf, value.unwrapKey().orElseThrow());
				}
			}
		}
	}
}
