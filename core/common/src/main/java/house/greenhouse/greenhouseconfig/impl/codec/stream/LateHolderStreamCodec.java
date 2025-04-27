package house.greenhouse.greenhouseconfig.impl.codec.stream;

import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

public class LateHolderStreamCodec<T> implements StreamCodec<ByteBuf, Holder<T>> {
	private final StreamCodec<ByteBuf, ResourceKey<T>> holderCodec;

	public LateHolderStreamCodec(ResourceKey<? extends Registry<T>> registry) {
		holderCodec = ResourceKey.streamCodec(registry);
	}

	@Override
	public @NotNull Holder<T> decode(@NotNull ByteBuf buf) {
		ResourceKey<T> resourceKey = holderCodec.decode(buf);
		return new LateHolderImpl<>(resourceKey.registryKey(), resourceKey);
	}

	@Override
	public void encode(@NotNull ByteBuf buf, @NotNull Holder<T> holder) {
		if (!(holder instanceof LateHolder<T> lateHolder))
			holderCodec.encode(buf, holder.unwrapKey().orElseThrow());
		else
			holderCodec.encode(buf, lateHolder.key());
	}
}
