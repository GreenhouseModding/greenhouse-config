package house.greenhouse.greenhouseconfig.api.codec;

import com.mojang.datafixers.util.Either;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderImpl;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GreenhouseConfigStreamCodecs {
    /**
     *
     * @param registry
     * @return
     * @param <T>
     */
    public static <T> StreamCodec<ByteBuf, LateHolder<T>> lateHolderStreamCodec(final ResourceKey<Registry<T>> registry) {
        StreamCodec<ByteBuf, Holder<T>> holder = new StreamCodec<>() {
            private final StreamCodec<ByteBuf, ResourceKey<T>> holderCodec = ResourceKey.streamCodec(registry);

            public Holder<T> decode(ByteBuf buf) {
                ResourceKey<T> resourceKey = holderCodec.decode(buf);
                return new LateHolderImpl<>(resourceKey.registryKey(), resourceKey);
            }

            public void encode(ByteBuf buf, Holder<T> holder) {
                if (!(holder instanceof LateHolder<T> lateHolder))
                    holderCodec.encode(buf, holder.unwrapKey().orElseThrow());
                else
                    holderCodec.encode(buf, lateHolder.key());
            }
        };
        return holder.map(holders -> (LateHolder<T>) holders, holders -> holders);
    }

    public static <T> StreamCodec<ByteBuf, LateHolderSet<T>> lateHolderSetStreamCodec(final ResourceKey<? extends Registry<T>> registryKey) {
        StreamCodec<ByteBuf, HolderSet<T>> holder = new StreamCodec<>() {
            private final StreamCodec<ByteBuf, ResourceKey<T>> holderCodec = ResourceKey.streamCodec(registryKey);

            public HolderSet<T> decode(ByteBuf buf) {
                int i = VarInt.read(buf) - 1;
                if (i == -1) {
                    return LateHolderSetImpl.createFromTags((ResourceKey<Registry<T>>) registryKey, List.of(TagKey.create(registryKey, ResourceLocation.STREAM_CODEC.decode(buf))));
                } else {
                    List<TagKey<T>> tags = new ArrayList<>(Math.min(i, 65536));
                    List<ResourceKey<T>> entries = new ArrayList<>(Math.min(i, 65536));

                    for (int j = 0; j < i; ++j) {
                        boolean isTag = buf.readBoolean();
                        if (isTag)
                            tags.add(TagKey.create(registryKey, ResourceLocation.STREAM_CODEC.decode(buf)));
                        else
                            entries.add(holderCodec.decode(buf));
                    }

                    return LateHolderSetImpl.createMixed((ResourceKey<Registry<T>>) registryKey, tags, entries);
                }
            }

            public void encode(ByteBuf buf, HolderSet<T> holderSet) {
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
        };
        return holder.map(holders -> (LateHolderSet<T>) holders, holders -> holders);
    }
}
